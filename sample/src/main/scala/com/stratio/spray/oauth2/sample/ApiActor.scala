/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.spray.oauth2.sample

import akka.actor.{Props, ActorLogging}
import com.stratio.spray.oauth2.client.{OauthClient, OauthClientHelper}
import spray.routing.HttpServiceActor

object ApiActor {
  def props = Props[ApiActor]
}

class ApiActor extends HttpServiceActor with OauthClient with ActorLogging {
import OauthClientHelper._

  val myRoutes = sealRoute {
    secured { user =>
      path("p1") {
        authorize(hasRole(Seq("*"), user)) {
          get {
            complete(s"inn at  p1 ")
          }
        }
      } ~
        path("p2") {
          authorize(hasRole(Seq("RoleWithoutPermision"), user)) {
            get {
              complete(s"inn at  p2")
            }
          }
        } ~
        path("p3") {
          get {
            complete(s"inn at  p3")
          }
        }
    }
  }

  val otherRoute = path("noSecured") {
    get {
      complete("no secured zone")
    }
  }
  val route = otherRoute ~ secRoute ~ myRoutes

  override def receive: Receive = runRoute(route)
}



