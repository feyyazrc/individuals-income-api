/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsincomeapi.services

import java.util.UUID

import org.joda.time.LocalDate.parse
import org.mockito.BDDMockito.given
import org.mockito.Matchers.{any, refEq}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualsincomeapi.connector.{DesConnector, IndividualsMatchingApiConnector}
import uk.gov.hmrc.individualsincomeapi.domain.SandboxIncomeData._
import uk.gov.hmrc.individualsincomeapi.domain._
import uk.gov.hmrc.individualsincomeapi.services.{LiveIncomeService, SandboxIncomeService}
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import unit.uk.gov.hmrc.individualsincomeapi.util.Dates

import scala.concurrent.Future.failed

class IncomeServiceSpec extends UnitSpec with MockitoSugar with ScalaFutures with Dates {

  trait Setup {
    implicit val hc = HeaderCarrier()

    val mockMatchingConnector = mock[IndividualsMatchingApiConnector]
    val mockDesConnector = mock[DesConnector]
    val liveIncomeService = new LiveIncomeService(mockMatchingConnector, mockDesConnector)
    val sandboxIncomeService = new SandboxIncomeService()
  }

  "liveIncomeService fetch income by matchId function" should {
    val matchedCitizen = MatchedCitizen(UUID.randomUUID(), Nino("AA100009B"))
    val interval = toInterval("2016-01-12", "2016-03-22")

    "return the employment's income" in new Setup {
      val desEmployments = Seq(DesEmployment(Seq(DesPayment(parse("2016-02-28"), 10.50))))

      given(mockMatchingConnector.resolve(matchedCitizen.matchId)).willReturn(matchedCitizen)
      given(mockDesConnector.fetchEmployments(refEq(matchedCitizen.nino), refEq(interval))(any())).willReturn(desEmployments)

      val result = await(liveIncomeService.fetchIncomeByMatchId(matchedCitizen.matchId, interval)(hc))

      result shouldBe List(Payment(10.5, parse("2016-02-28")))
    }

    "Sort the payments by payment date descending" in new Setup {
      val desEmployments = Seq(DesEmployment(Seq(DesPayment(parse("2016-02-28"), 10.50), DesPayment(parse("2016-04-28"), 10.50), DesPayment(parse("2016-03-28"), 10.50))))

      given(mockMatchingConnector.resolve(matchedCitizen.matchId)).willReturn(matchedCitizen)
      given(mockDesConnector.fetchEmployments(refEq(matchedCitizen.nino), refEq(interval))(any())).willReturn(desEmployments)

      val result = await(liveIncomeService.fetchIncomeByMatchId(matchedCitizen.matchId, interval)(hc))

      result shouldBe List(Payment(10.5, parse("2016-04-28")), Payment(10.5, parse("2016-03-28")), Payment(10.5, parse("2016-02-28")))
    }

    "Return empty list when there are no payments for a given period" in new Setup {
      given(mockMatchingConnector.resolve(matchedCitizen.matchId)).willReturn(matchedCitizen)
      given(mockDesConnector.fetchEmployments(refEq(matchedCitizen.nino), refEq(interval))(any())).willReturn(Seq.empty)

      val result = await(liveIncomeService.fetchIncomeByMatchId(matchedCitizen.matchId, interval)(hc))

      result shouldBe List.empty
    }

    "propagate MatchNotFoundException when the matchId does not exist" in new Setup {

      given(mockMatchingConnector.resolve(matchedCitizen.matchId)).willThrow(new MatchNotFoundException)

      intercept[MatchNotFoundException]{await(liveIncomeService.fetchIncomeByMatchId(matchedCitizen.matchId, interval)(hc))}
    }

    "fail when DES returns an error" in new Setup {

      given(mockMatchingConnector.resolve(matchedCitizen.matchId)).willReturn(matchedCitizen)
      given(mockDesConnector.fetchEmployments(refEq(matchedCitizen.nino), refEq(interval))(any())).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException]{await(liveIncomeService.fetchIncomeByMatchId(matchedCitizen.matchId, interval)(hc))}
    }
  }

  "SandboxIncomeService fetch income by matchId function" should {

    "return income for the entire available history ordered by date descending" in new Setup {

      val expected = List(
        Payment(500.25, parse("2017-02-16"), Some(EmpRef.fromIdentifiers("123/DI45678")), None, Some(46)),
        Payment(500.25, parse("2017-02-09"), Some(EmpRef.fromIdentifiers("123/DI45678")), None, Some(45)),
        Payment(1000.25, parse("2016-05-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(2), None),
        Payment(1000.25, parse("2016-04-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(1), None),
        Payment(1000.5, parse("2016-03-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(12), None),
        Payment(1000.5, parse("2016-02-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(11), None),
        Payment(1000.5, parse("2016-01-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(10), None))

      val result = await(sandboxIncomeService.fetchIncomeByMatchId(sandboxMatchId, toInterval("2016-01-01", "2017-03-01"))(hc))
      result shouldBe expected
    }

    "return income for a limited period" in new Setup {

      val expected = List(
        Payment(1000.25, parse("2016-05-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(2), None),
        Payment(1000.25, parse("2016-04-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(1), None),
        Payment(1000.5, parse("2016-03-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(12), None),
        Payment(1000.5, parse("2016-02-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(11), None),
        Payment(1000.5, parse("2016-01-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), Some(10), None))

      val result = await(sandboxIncomeService.fetchIncomeByMatchId(sandboxMatchId, toInterval("2016-01-01", "2016-07-01"))(hc))
      result shouldBe expected
    }

    "return correct income when range includes a period of no payments" in new Setup {

      val expected = List(
        Payment(500.25, parse("2017-02-09"), Some(EmpRef.fromIdentifiers("123/DI45678")), weekPayNumber = Some(45)),
        Payment(1000.25, parse("2016-05-28"), Some(EmpRef.fromIdentifiers("123/AI45678")), monthPayNumber = Some(2)))

      val result = await(sandboxIncomeService.fetchIncomeByMatchId(sandboxMatchId, toInterval("2016-04-30", "2017-02-15"))(hc))

      result shouldBe expected
    }

    "return no income when the individual has no income for a given period" in new Setup {

      val result = await(sandboxIncomeService.fetchIncomeByMatchId(sandboxMatchId, toInterval("2016-08-01", "2016-09-01"))(hc))

      result shouldBe Seq.empty
    }

    "throw not found exception when no individual exists for the given matchId" in new Setup {
      intercept[MatchNotFoundException](
        await(sandboxIncomeService.fetchIncomeByMatchId(UUID.randomUUID(), toInterval("2016-01-01", "2018-03-01"))(hc)))
    }
  }
}
