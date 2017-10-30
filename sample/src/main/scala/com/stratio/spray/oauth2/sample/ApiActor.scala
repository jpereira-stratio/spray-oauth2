/*
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache License 2.0. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the terms of the License for more details.
 *
 * SPDX-License-Identifier: Apache-2.0.
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

  val myRoutes = {
    {
      path("p1") {
        get {
          complete(s"inn at  p1 ")
        }
      } ~
        path("p2") {
          secured { user =>
            authorize(hasRole(Seq("RoleWithoutPermision"), user)) {
              get {
                complete(s"inn at  p2")
              }
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
  val authorizedRoute = authorized { user =>
    path("authorized") {
      get {
        complete("no secured zone")
      }
    }
  }
  val route = otherRoute ~ secRoute ~ myRoutes ~ authorizedRoute

  override def receive: Receive = runRoute(route)
}
