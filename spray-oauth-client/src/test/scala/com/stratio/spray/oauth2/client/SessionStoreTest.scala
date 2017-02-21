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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class SessionStoreTest extends FlatSpec with Matchers {

  import SessionStore._

  val now = System.currentTimeMillis()
  val alive = 10000
  val dead = -10000
  "Session Store" should "add a session" in {

    addSession("1", "my session content", alive)
    addSession("2", "my session content2", dead)
    addSession("3", "my session content3", dead)

    sessionStore.get("1").map(_._1) should be(Some("my session content"))
  }
  it should "not retrieve a expired session" in {
    getSession("2") should be(None)
  }
  it should "retrieve session" in {
    getSession("1") should be(Some("my session content"))
  }
  it should "remove the session" in {
    removeSession("1")
    sessionStore.get("1") should be(None)
  }
  it should "return a random id" in {
    getRandomSessionId.isEmpty should be(false)
  }
  it should "clean all dead sessions" in {
    clean
    sessionStore.get("3") should be (None)
  }
}
