/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
