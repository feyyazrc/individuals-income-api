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

package uk.gov.hmrc.individualsincomeapi.domain.v2

import play.api.libs.json.Json
import uk.gov.hmrc.individualsincomeapi.domain.TaxYear
import uk.gov.hmrc.individualsincomeapi.domain.integrationframework.IfSaEntry

case class SaOtherIncomeRecords(taxReturns: Seq[SaOtherIncomeRecordsTaxReturn])

object SaOtherIncomeRecords {

  implicit val saOtherIncomeRecordsJsonFormat = Json.format[SaOtherIncomeRecords]

  def transform(ifSaEntry: Seq[IfSaEntry]): SaOtherIncomeRecords =
    SaOtherIncomeRecords(transformSaOtherIncomeTaxReturn(ifSaEntry))

  private def default = SaOtherIncomeRecord(0.0, None)

  private def transformSaOtherIncomeRecord(entry: IfSaEntry) =
    entry.returnList match {
      case Some(list) => {
        list.map { entry =>
          entry.income match {
            case Some(value) =>
              SaOtherIncomeRecord(value.other.getOrElse(0.0), entry.utr)
            case _ => default
          }
        }
      }
      case _ => Seq(default)
    }

  private def transformSaOtherIncomeTaxReturn(ifSaEntry: Seq[IfSaEntry]) =
    ifSaEntry
      .flatMap { entry =>
        entry.taxYear.map { ty =>
          SaOtherIncomeRecordsTaxReturn(
            TaxYear.fromEndYear(ty.toInt).formattedTaxYear,
            transformSaOtherIncomeRecord(entry)
          )
        }
      }
      .sortBy(_.taxYear)

}
