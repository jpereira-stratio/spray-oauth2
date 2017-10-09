/**
  * Copyright (C) 2016 Stratio (http://stratio.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.stratio.akka.oauth2.client

import java.util.UUID

import scala.Predef._
import scala.collection.immutable

object SessionStore {

  var sessionStore: Map[String, (String, Long)] = Map.empty[String, (String, Long)]

  def addSession(sessionId: String, identity: String, expires: Long): Unit = {
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

  def removeSession(sessionId: String): Unit = {
    synchronized {
      sessionStore -= sessionId
    }
  }

  def clean: immutable.Iterable[Option[String]] = {
    sessionStore.map{case (id,(identity, expires)) => validateSession(identity,expires,id)}
  }

  def now: Long = System.currentTimeMillis

  def getRandomSessionId: String = UUID.randomUUID().toString
}


