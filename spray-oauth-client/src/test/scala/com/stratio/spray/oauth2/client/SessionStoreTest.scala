package com.stratio.spray.oauth2.client

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class SessionStoreTest extends FlatSpec with Matchers with SessionStore {
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
