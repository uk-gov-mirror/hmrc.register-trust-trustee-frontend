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

package controllers.register.leadtrustee.individual

import config.FrontendAppConfig
import config.annotations.LeadTrusteeIndividual
import controllers.actions._
import controllers.actions.register.TrusteeNameRequest
import controllers.actions.register.leadtrustee.individual.NameRequiredActionImpl
import forms.NinoFormProvider
import models.{IssueBuildingPayloadResponse, ServiceNotIn5mldModeResponse, SuccessfulMatchResponse, TrustsIndividualCheckServiceResponse, UnsuccessfulMatchResponse, UserAnswers}
import navigation.Navigator
import pages.register.leadtrustee.individual.{FailedMatchingPage, TrusteesNinoPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import services.TrustsIndividualCheckService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.leadtrustee.individual.NinoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class NinoController @Inject()(
                                override val messagesApi: MessagesApi,
                                implicit val frontendAppConfig: FrontendAppConfig,
                                registrationsRepository: RegistrationsRepository,
                                @LeadTrusteeIndividual navigator: Navigator,
                                standardActionSets: StandardActionSets,
                                nameAction: NameRequiredActionImpl,
                                formProvider: NinoFormProvider,
                                val controllerComponents: MessagesControllerComponents,
                                view: NinoView,
                                service: TrustsIndividualCheckService
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[String] = formProvider("leadTrustee.individual.nino")

  private def actions(index: Int, draftId: String): ActionBuilder[TrusteeNameRequest, AnyContent] =
    standardActionSets.indexValidated(draftId, index) andThen nameAction(index)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(TrusteesNinoPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index, request.trusteeName))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index,draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, request.trusteeName))),

        value => {
          for {
            answersWithNinoUpdated <- Future.fromTry(request.userAnswers.set(TrusteesNinoPage(index), value))
            matchingResponse <- service.matchLeadTrustee(answersWithNinoUpdated, index)
            answersWithFailedAttemptsUpdated <- updateFailedAttempts(matchingResponse, answersWithNinoUpdated, index)
            _ <- registrationsRepository.set(answersWithFailedAttemptsUpdated)
          } yield Redirect {
            matchingResponse match {
              case SuccessfulMatchResponse | ServiceNotIn5mldModeResponse =>
                navigator.nextPage(TrusteesNinoPage(index), draftId, answersWithFailedAttemptsUpdated)
              case UnsuccessfulMatchResponse =>
                routes.FailedMatchingController.onPageLoad(index, draftId)
              case IssueBuildingPayloadResponse =>
                routes.NameController.onPageLoad(index, draftId)
            }
          }
        }
      )
  }

  private def updateFailedAttempts(matchingResponse: TrustsIndividualCheckServiceResponse,
                                   userAnswers: UserAnswers,
                                   index: Int): Future[UserAnswers] = {
    matchingResponse match {
      case UnsuccessfulMatchResponse =>
        val failedAttempts = userAnswers.get(FailedMatchingPage(index)).getOrElse(0)
        for {
          updatedFailedAttempts <- Future.fromTry(userAnswers.set(FailedMatchingPage(index), failedAttempts + 1))
        } yield {
          updatedFailedAttempts
        }
      case _ =>
        Future.fromTry(Success(userAnswers))
    }
  }
}
