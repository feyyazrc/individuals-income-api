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

package it.uk.gov.hmrc.individualsincomeapi.cache.v2

import org.mongodb.scala.model.Filters

import java.util.UUID
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsString, Json, OFormat}
import uk.gov.hmrc.individualsincomeapi.cache.v2.ShortLivedCache
import uk.gov.hmrc.integration.ServiceSpec
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import utils.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global

class ShortLivedCacheSpec
    extends AnyWordSpec with Matchers with MongoSpecSupport with ServiceSpec with BeforeAndAfterEach with TestSupport {

  val cacheTtl = 60
  val id = UUID.randomUUID().toString
  val cachekey = "test-class-key-v1"
  val testValue = TestClass("one", "two")

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri, "cache.ttlInSeconds" -> cacheTtl)
    .bindings(bindModules: _*)
    .build()

  val shortLivedCache = fakeApplication.injector.instanceOf[ShortLivedCache]

  def externalServices: Seq[String] = Seq.empty

  override def beforeEach() {

    super.beforeEach()
    await(shortLivedCache.collection.drop().toFuture())

  }

  override def afterEach() {

    super.afterEach()
    await(shortLivedCache.collection.drop().toFuture())

  }

  "cache" should {

    "store the encrypted version of a value" in {

      await(shortLivedCache.cache(id, testValue)(TestClass.format))
      retrieveRawCachedValue(id, cachekey) shouldBe JsString("6aZpkTxkw3C4e5xTyfy3Lf/OZOFz+GcaSkeFI++0HOs=")

    }
  }

  "fetch" should {

    "retrieve the unencrypted cached value for a given id and key" in {

      await(shortLivedCache.cache(id, testValue)(TestClass.format))
      await(shortLivedCache.fetchAndGetEntry[TestClass](id)(TestClass.format)) shouldBe Some(testValue)

    }

    "return None if no cached value exists for a given id and key" in {

      await(shortLivedCache.fetchAndGetEntry[TestClass](id)(TestClass.format)) shouldBe None

    }
  }

  private def retrieveRawCachedValue(id: String, key: String) = {

    await(shortLivedCache.collection.find(Filters.equal("id", toBson(id)))
      .headOption
      .map {
        case Some(entry) => entry.data.individualsIncome
        case None => None
      })

  }

  case class TestClass(one: String, two: String)

  object TestClass {

    implicit val format: OFormat[TestClass] = Json.format[TestClass]

  }
}
