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

// $COVERAGE-OFF$

package uk.gov.hmrc.individualsincomeapi.controllers

import controllers.Assets
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.individualsincomeapi.views._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class DocumentationController @Inject()(
  cc: ControllerComponents,
  assets: Assets,
  config: Configuration)
    extends BackendController(cc) {

  private val v1WhitelistedApplicationIDs = config
    .getOptional[Seq[String]](
      "api.access.version-P1.0.whitelistedApplicationIds"
    )
    .getOrElse(Seq.empty)

  private val v2WhitelistedApplicationIDs = config
    .getOptional[Seq[String]](
      "api.access.version-2.0.whitelistedApplicationIds"
    )
    .getOrElse(Seq.empty)

  private lazy val v2EndpointsEnabled: Boolean =
    config
      .getOptional[Boolean]("api.access.version-2.0.endpointsEnabled")
      .getOrElse(true)

  private lazy val v2Status: String =
    config
      .getOptional[String]("api.access.version-2.0.status")
      .getOrElse("BETA")

  def definition(): Action[AnyContent] = Action {
    Ok(txt.definition(v1WhitelistedApplicationIDs, v2WhitelistedApplicationIDs, v2EndpointsEnabled, v2Status))
      .withHeaders(CONTENT_TYPE -> JSON)
  }
  def documentation(
    version: String,
    endpointName: String
  ): Action[AnyContent] =
    assets.at(s"/public/api/documentation/$version", s"${endpointName.replaceAll(" ", "-")}.xml")

  def raml(version: String, file: String) =
    assets.at(s"/public/api/conf/$version", file)

}

// $COVERAGE-ON$
