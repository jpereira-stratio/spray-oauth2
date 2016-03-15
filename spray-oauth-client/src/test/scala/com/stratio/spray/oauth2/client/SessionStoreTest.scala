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
class SessionStoreTest extends FlatSpec with Matchers  {
  import SessionStore._
  "Session Store" should "add a session" in {
    addSession("1", "my session content")
    sessionStore.get("1") should be(Some("my session content"))
  }
  it should "retrieve session" in {
    getSession("1") should be(Some("my session content"))
  }
  it should "remove the session" in{
    removeSession("1")
    sessionStore.get("1") should be (None)
  }
  it should "return a random id" in {
    getRandomSessionId.isEmpty should be (false)
  }
}
