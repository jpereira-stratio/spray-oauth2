/*
 *  © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *  This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.spray.oauth2.client

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.stratio.akka.oauth2.client.OauthClient
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

class AkkaOauthSpec extends FlatSpec with OauthClient with Matchers with ScalatestRouteTest {

  override implicit val system = ActorSystem()
  implicit val actorRefFactory: ActorRefFactory = system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val timeout: Timeout = Timeout(30.seconds)

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

  "Is logged " should "return " in {
    Get("/isLogged") ~> isLogged ~> check {
      status should be(Found)
    }
  }

}
