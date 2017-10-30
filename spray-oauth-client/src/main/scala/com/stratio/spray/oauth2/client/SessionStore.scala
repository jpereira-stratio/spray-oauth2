/*
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache License 2.0. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the terms of the License for more details.
 *
 * SPDX-License-Identifier: Apache-2.0.
 */
package com.stratio.spray.oauth2.client

import java.util.UUID

import scala.Predef._

object SessionStore {

  var sessionStore: Map[String, (String, Long)] = Map.empty[String, (String, Long)]

  def addSession(sessionId: String, identity: String, expires: Long) = {
    synchronized {
      sessionStore += sessionId -> (identity, now + expires)
    }
  }

  private def validateSession(identity: String, expires: Long, sessionId: String) = {
    if (expires < now) {
      removeSession(sessionId)
      None
    } else Option(identity)
  }

  def getSession(sessionId: String): Option[String] = {
    synchronized {
      sessionStore.get(sessionId) match {
        case Some((identity, expires)) => validateSession(identity, expires, sessionId)
        case _ => None
      }
    }
  }

  def removeSession(sessionId: String) = {
    synchronized {
      sessionStore -= sessionId
    }
  }

  def clean = {
    sessionStore.map{case (id,(identity, expires)) => validateSession(identity,expires,id)}
  }

  def now: Long = System.currentTimeMillis

  def getRandomSessionId: String = UUID.randomUUID().toString
}
