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

package views.register.leadtrustee.organisation

import forms.UKAddressFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.UkAddressViewBehaviours
import views.html.register.leadtrustee.organisation.UkAddressView

class UkAddressViewSpec extends UkAddressViewBehaviours {

  val prefix = "leadTrustee.organisation.ukAddress"
  val fakeName = "Test"
  val index = 0

  override val form = new UKAddressFormProvider()()

  "UkAddress View" must {

    val view = viewFor[UkAddressView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, fakeDraftId, index, fakeName)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), prefix, fakeName)

    behave like pageWithBackLink(applyView(form))

    behave like ukAddressPage(applyView, Some(prefix), fakeName)

    behave like pageWithASubmitButton(applyView(form))

  }
}
