/*
 * Copyright 2020 HM Revenue & Customs
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

package repositories

import base.SpecBase
import models.RegistrationSubmission.AnswerSection
import models.Status.{Completed, InProgress}
import models.{Status, UserAnswers}
import models.registration.pages.AddATrustee
import pages.entitystatus.TrusteeStatus
import pages.register.trustees.{AddATrusteePage, IsThisLeadTrusteePage}

import scala.collection.immutable.Nil

class SubmissionSetFactorySpec extends SpecBase {

  "Submission set factory" must {

    val factory = injector.instanceOf[SubmissionSetFactory]

    "return None status if no trustees" in {
      factory.trusteesStatus(emptyUserAnswers) mustBe None
    }

    "return InProgress status" when {

      "there are trustees that are incomplete" in {

        val userAnswers = emptyUserAnswers
          .set(IsThisLeadTrusteePage(0), true).success.value
          .set(IsThisLeadTrusteePage(1), false).success.value
          .set(TrusteeStatus(1), Status.Completed).success.value

        factory.trusteesStatus(userAnswers).value mustBe InProgress
      }

      "there are trustees that are complete, but section flagged not complete" in {
        val userAnswers = emptyUserAnswers
          .set(IsThisLeadTrusteePage(0), true).success.value
          .set(TrusteeStatus(0), Status.Completed).success.value
          .set(IsThisLeadTrusteePage(1), false).success.value
          .set(TrusteeStatus(1), Status.Completed).success.value
          .set(AddATrusteePage, AddATrustee.YesLater).success.value

        factory.trusteesStatus(userAnswers).value mustBe InProgress
      }

      "there are completed trustees, the section is flagged as completed, but there is no lead trustee" in {
        val userAnswers = emptyUserAnswers
          .set(IsThisLeadTrusteePage(0), false).success.value
          .set(TrusteeStatus(0), Status.Completed).success.value
          .set(IsThisLeadTrusteePage(1), false).success.value
          .set(TrusteeStatus(1), Status.Completed).success.value
          .set(AddATrusteePage, AddATrustee.NoComplete).success.value

        factory.trusteesStatus(userAnswers).value mustBe InProgress
      }
    }

    "return Completed status" when {

      "there are trustees that are complete, and section flagged as complete" in {

        val userAnswers = emptyUserAnswers
          .set(IsThisLeadTrusteePage(0), true).success.value
          .set(TrusteeStatus(0), Status.Completed).success.value
          .set(IsThisLeadTrusteePage(1), false).success.value
          .set(TrusteeStatus(1), Status.Completed).success.value
          .set(AddATrusteePage, AddATrustee.NoComplete).success.value

        factory.trusteesStatus(userAnswers).value mustBe Completed
      }
    }

    "return no answer sections if no completed beneficiaries" in {

      factory.answerSectionsIfCompleted(emptyUserAnswers, Some(InProgress))
        .mustBe(Nil)
    }

//    "return completed answer sections" when {
//
//      "only one beneficiary" must {
//        "have 'Beneficiaries' as section key" when {
//          "individual beneficiary only" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(IndividualBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Individual beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                )
//              )
//          }
//
//          "class of beneficiary only" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(ClassBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Class of beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                )
//              )
//          }
//
//          "charity beneficiary only" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(CharityBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Charity beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                )
//              )
//          }
//
//          "trust beneficiary only" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(TrustBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Trust beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                )
//              )
//          }
//
//          "company beneficiary only" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(CompanyBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Company beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                )
//              )
//          }
//
//          "large beneficiary only" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(LargeBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Employment related beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                )
//              )
//          }
//
//          "other beneficiary only" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(OtherBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Other beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                )
//              )
//          }
//        }
//      }
//
//      "more than one beneficiary" must {
//        "have 'Beneficiaries' as section key of the topmost section" when {
//          "individual beneficiary and class of beneficiary" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(IndividualBeneficiaryStatus(0), Completed).success.value
//              .set(ClassBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Individual beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                ),
//                AnswerSection(
//                  Some("Class of beneficiary 1"),
//                  Nil,
//                  None
//                )
//              )
//          }
//
//          "class of beneficiary and trust beneficiary" in {
//            val userAnswers: UserAnswers = emptyUserAnswers
//              .set(ClassBeneficiaryStatus(0), Completed).success.value
//              .set(TrustBeneficiaryStatus(0), Completed).success.value
//
//            factory.answerSectionsIfCompleted(userAnswers, Some(Completed)) mustBe
//              List(
//                AnswerSection(
//                  Some("Class of beneficiary 1"),
//                  Nil,
//                  Some("Beneficiaries")
//                ),
//                AnswerSection(
//                  Some("Trust beneficiary 1"),
//                  Nil,
//                  None
//                )
//              )
//          }
//        }
//      }
//
//    }
  }

}
