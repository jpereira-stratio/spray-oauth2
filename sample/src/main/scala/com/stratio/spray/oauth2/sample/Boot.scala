/*
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache License 2.0. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the terms of the License for more details.
 *
 * SPDX-License-Identifier: Apache-2.0.
 */
package com.stratio.spray.oauth2.sample

import akka.actor.ActorSystem
import akka.io.IO
import spray.can.Http

object Boot extends App{

  implicit val system = ActorSystem("demo")

  //	val settings = Settings(system)
  val api = system.actorOf(ApiActor.props, "api-actor")

  IO(Http) ! Http.Bind(listener = api,
    interface = "0.0.0.0",
    port = 9090)
}
