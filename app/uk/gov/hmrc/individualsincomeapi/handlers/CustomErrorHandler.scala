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

package uk.gov.hmrc.individualsincomeapi.handlers

import javax.inject.Inject
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.libs.json.Json
import play.api.mvc.Results.Status
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.individualsincomeapi.domain.{ErrorInvalidRequest, ErrorNotFound}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.backend.http.{ErrorResponse, JsonErrorHandler}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomErrorHandler @Inject()(
  auditConnector: AuditConnector,
  httpAuditEvent: HttpAuditEvent,
  configuration: Configuration)
    extends JsonErrorHandler(auditConnector, httpAuditEvent, configuration) {

  import httpAuditEvent.dataEvent

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    implicit val headerCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    statusCode match {
      case NOT_FOUND =>
        auditConnector.sendEvent(dataEvent("ResourceNotFound", "Resource Endpoint Not Found", request))
        Future.successful(ErrorNotFound.toHttpResponse)
      case BAD_REQUEST =>
        auditConnector.sendEvent(dataEvent("ServerValidationError", "Request bad format exception", request))
        Future.successful(ErrorInvalidRequest(message).toHttpResponse)
      case _ =>
        auditConnector.sendEvent(dataEvent("ClientError", s"A client error occurred, status: $statusCode", request))
        Future.successful(Status(statusCode)(Json.toJson(ErrorResponse(statusCode, message))))
    }
  }

}
