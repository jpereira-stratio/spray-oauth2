/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.spray.oauth2.client

import java.util.UUID

import scala.Predef._

trait SessionStore  {

  var sessionStore: Map[String, String] = Map()

  def addSession(sessionId: String, identity: String) ={
      sessionStore += sessionId -> identity
  }

  def getSession(sessionId: String): Option[String] ={
    sessionStore.get(sessionId)
  }


  def removeSession(sessionId: String) ={
    sessionStore -= sessionId
  }

  def getRandomSessionId:String = UUID.randomUUID().toString

}



