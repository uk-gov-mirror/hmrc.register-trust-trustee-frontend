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

package controllers

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.actions.register.RegistrationIdentifierAction
import play.api.Logger.logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.Session

import scala.concurrent.ExecutionContext

@Singleton
class LogoutController @Inject()(
                                  appConfig: FrontendAppConfig,
                                  val controllerComponents: MessagesControllerComponents,
                                  identify: RegistrationIdentifierAction,
                                  auditConnector: AuditConnector
                                )(implicit val ec: ExecutionContext) extends FrontendBaseController {

  def logout: Action[AnyContent] = identify { request =>

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    logger.info(s"[Session ID: ${utils.Session.id(hc)}] user signed out from the service, asking for feedback")

    val auditData = Map(
      "sessionId" -> Session.id(hc),
      "event" -> "signout",
      "service" -> "register-trust-trustee-frontend",
      "userGroup" -> request.affinityGroup.toString
    )

    auditConnector.sendExplicitAudit(
      "trusts",
      auditData
    )

    Redirect(appConfig.logoutUrl).withSession(session = ("feedbackId", Session.id(hc)))

  }
}