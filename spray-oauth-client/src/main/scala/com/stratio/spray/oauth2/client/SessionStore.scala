/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.spray.oauth2.client

import java.util.UUID

import scala.Predef._

object SessionStore  {

  var sessionStore: Map[String, String] = Map.empty[String,String]

  def addSession(sessionId: String, identity: String) ={
    synchronized {
      sessionStore += sessionId -> identity
    }
  }

  def getSession(sessionId: String): Option[String] ={
    synchronized {
      sessionStore.get(sessionId)
    }
  }

  def removeSession(sessionId: String) ={
    synchronized {
      sessionStore -= sessionId
    }
  }

  def getRandomSessionId:String = UUID.randomUUID().toString
}


