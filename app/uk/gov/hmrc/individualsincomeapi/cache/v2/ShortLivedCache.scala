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

package uk.gov.hmrc.individualsincomeapi.cache.v2

import javax.inject.{Inject, Singleton}
import play.api.Configuration

import uk.gov.hmrc.individualsincomeapi.cache.{CacheRepository => BaseCache}
import uk.gov.hmrc.mongo.MongoComponent
import scala.concurrent.ExecutionContext

@Singleton
class ShortLivedCache @Inject()(
                                 override val cacheConfig: CacheRepositoryConfiguration, configuration: Configuration, mongo: MongoComponent)
                               (implicit ec: ExecutionContext)
  extends BaseCache(cacheConfig, configuration, mongo)