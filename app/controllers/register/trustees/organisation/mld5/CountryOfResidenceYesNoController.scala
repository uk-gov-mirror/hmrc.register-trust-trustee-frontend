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

package controllers.register.trustees.organisation.mld5

import config.FrontendAppConfig
import config.annotations.TrusteeOrganisation
import controllers.actions.StandardActionSets
import controllers.actions.register.trustees.organisation.NameRequiredActionImpl
import controllers.actions.register.trustees.organisation.NameRequiredAction
import forms.YesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.register.trustees.organisation.mld5.CountryOfResidenceYesNoPage
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.register.trustees.organisation.mld5.CountryOfResidenceYesNoView
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class CountryOfResidenceYesNoController @Inject()(
                                                   val controllerComponents: MessagesControllerComponents,
                                                   implicit val frontendAppConfig: FrontendAppConfig,
                                                   repository: RegistrationsRepository,
                                                   @TrusteeOrganisation navigator: Navigator,
                                                   actions: StandardActionSets,
                                                   requireName: NameRequiredActionImpl,
                                                   formProvider: YesNoFormProvider,
                                                   view: CountryOfResidenceYesNoView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = formProvider.withPrefix("trustees.organisation.5mld.countryOfResidenceYesNo")

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] =
    actions.identifiedUserWithData(draftId).andThen(requireName(index)) {
      implicit request =>

        val preparedForm = request.userAnswers.get(CountryOfResidenceYesNoPage(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, draftId, index, request.trusteeName))
    }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] =
    actions.identifiedUserWithData(draftId).andThen(requireName(index)).async {
      implicit request =>

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, draftId, index, request.trusteeName))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(CountryOfResidenceYesNoPage(index), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CountryOfResidenceYesNoPage(index), draftId, updatedAnswers))
        )
    }
}
