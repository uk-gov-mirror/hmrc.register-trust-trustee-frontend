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

package views.register.trustees.individual

import forms.YesNoFormProvider
import models.core.pages.FullName
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.trustees.individual.IDCardDetailsYesNoView

class IdCardDetailsYesNoViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "trustee.individual.idCardDetailsYesNo"

  val index = 0

  val form = new YesNoFormProvider().withPrefix("trustee.individual.idCardDetailsYesNo")

  val name = FullName("FirstName", None, "LastName").toString

  "idCardDetailsYesNo view" must {

    val view = viewFor[IDCardDetailsYesNoView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, fakeDraftId, index, name)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, None, Seq(name))

    behave like pageWithASubmitButton(applyView(form))

  }
}
