/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.register

import config.FrontendAppConfig
import controllers.actions._
import controllers.actions.register.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.IndividualOrBusinessFormProvider
import models.Enumerable
import models.core.pages.TrusteeOrLeadTrustee.LeadTrustee
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.register.{TrusteeIndividualOrBusinessPage, TrusteeOrLeadTrusteePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import sections.Trustees
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.TrusteeIndividualOrBusinessView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TrusteeIndividualOrBusinessController @Inject()(
                                                       override val messagesApi: MessagesApi,
                                                       implicit val frontendAppConfig: FrontendAppConfig,
                                                       registrationsRepository: RegistrationsRepository,
                                                       navigator: Navigator,
                                                       identify: RegistrationIdentifierAction,
                                                       getData: DraftIdRetrievalActionProvider,
                                                       requireData: RegistrationDataRequiredAction,
                                                       validateIndex: IndexActionFilterProvider,
                                                       formProvider: IndividualOrBusinessFormProvider,
                                                       requiredAnswer: RequiredAnswerActionProvider,
                                                       val controllerComponents: MessagesControllerComponents,
                                                       view: TrusteeIndividualOrBusinessView
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport
  with Enumerable.Implicits {

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen
      requireData andThen
      validateIndex(index, Trustees) andThen
      requiredAnswer(RequiredAnswer(TrusteeOrLeadTrusteePage(index), routes.TrusteeOrLeadTrusteeController.onPageLoad(index, draftId)))

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val form = formProvider(messagePrefix(index))

      val preparedForm = request.userAnswers.get(TrusteeIndividualOrBusinessPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index, heading(index)))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      val form = formProvider(messagePrefix(index))

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, heading(index)))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TrusteeIndividualOrBusinessPage(index), value))
            _ <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(TrusteeIndividualOrBusinessPage(index), draftId, updatedAnswers))
        }
      )
  }

  private def heading(index: Int)(implicit request: RegistrationDataRequest[AnyContent]): String = {
    val prefix = messagePrefix(index)
    Messages(s"$prefix.heading")
  }

  private def messagePrefix(index: Int)(implicit request: RegistrationDataRequest[AnyContent]): String = {
    val isLead = request.userAnswers.get(TrusteeOrLeadTrusteePage(index)).contains(LeadTrustee)
    val prefix = if (isLead) "leadTrustee" else "trustee"
    s"$prefix.individualOrBusiness"
  }

}
