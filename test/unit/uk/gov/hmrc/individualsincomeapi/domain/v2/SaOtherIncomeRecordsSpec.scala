/*
 * Copyright 2022 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualsincomeapi.domain.v2

import play.api.libs.json.Json
import uk.gov.hmrc.individualsincomeapi.domain.v2.SaOtherIncomeRecords
import utils.IncomeSaHelpers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SaOtherIncomeRecordsSpec extends AnyWordSpec with Matchers with IncomeSaHelpers {

  val ifSa = Seq(createValidSaTaxYearEntry())
  val ifSaNoData = Seq(createValidSaTaxYearEntryNoDataContainers())
  val ifSaNoValues = Seq(createValidSaTaxYearEntryNoValues())

  "SaOtherIncomeRecords" should {
    "Write to Json for verbose data" in {
      val expectedJson = Json.parse("""{
                                      |  "taxReturns": [
                                      |    {
                                      |      "taxYear": "2019-20",
                                      |      "other": [
                                      |        {
                                      |          "otherIncome": 100,
                                      |          "utr": "1234567890"
                                      |        }
                                      |      ]
                                      |    }
                                      |  ]
                                      |}""".stripMargin)

      val result = Json.toJson(SaOtherIncomeRecords.transform(ifSa))

      result shouldBe expectedJson
    }

    "Write to Json for no root data" in {
      val expectedJson = Json.parse("""{
                                      |  "taxReturns": []
                                      |}""".stripMargin)

      val result = Json.toJson(SaOtherIncomeRecords.transform(Seq()))

      result shouldBe expectedJson
    }

    "Write to Json with defaults no data containers" in {

      val expectedJson = Json.parse("""{
                                      |  "taxReturns": [
                                      |    {
                                      |      "taxYear": "2019-20",
                                      |      "other": [
                                      |        {
                                      |          "otherIncome": 0.0
                                      |        }
                                      |      ]
                                      |    }
                                      |  ]
                                      |}""".stripMargin)

      val result = Json.toJson(SaOtherIncomeRecords.transform(ifSaNoData))

      result shouldBe expectedJson
    }

    "Write to Json with defaults no vales" in {

      val expectedJson = Json.parse("""{
                                      |  "taxReturns": [
                                      |    {
                                      |      "taxYear": "2019-20",
                                      |      "other": [
                                      |        {
                                      |          "otherIncome": 0.0,
                                      |          "utr": "1234567890"
                                      |        }
                                      |      ]
                                      |    }
                                      |  ]
                                      |}""".stripMargin)

      val result = Json.toJson(SaOtherIncomeRecords.transform(ifSaNoValues))

      result shouldBe expectedJson
    }

  }
}
