/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
