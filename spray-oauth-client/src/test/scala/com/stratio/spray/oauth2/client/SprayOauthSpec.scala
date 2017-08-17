/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.spray.oauth2.client

import akka.actor.ActorRefFactory
import akka.util.Timeout
import org.scalatest.{FlatSpec, Matchers}
import spray.http.StatusCodes._
import spray.routing.RoutingSettings
import spray.testkit.ScalatestRouteTest
import scala.concurrent.duration._

class SprayOauthSpec extends FlatSpec with OauthClient with Matchers with ScalatestRouteTest {


  override implicit val actorRefFactory: ActorRefFactory = system


  implicit val timeout: Timeout = Timeout(30.seconds)

  implicit val _system = system


  "Login " should "return " in {
    Get("/login?code=asdfadsf") ~> login ~> check {
      status should be(Found)
    }
  }
  "Logout " should "return " in {
    Get("/logout") ~> logout ~> check {
      status should be(Found)
    }
  }
}
