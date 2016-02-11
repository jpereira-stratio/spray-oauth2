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
