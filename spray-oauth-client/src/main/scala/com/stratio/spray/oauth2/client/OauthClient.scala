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

import spray.client.pipelining._
import spray.http.{HttpResponse, HttpRequest, HttpCookie}
import spray.http.StatusCodes._
import spray.routing._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, ExecutionContext}
import SessionStore._
trait OauthClient extends HttpService   {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val configure = new Config

  import OauthClientHelper._

  def authorizeRedirect = redirect(authorizeRq, Found)

  def indexRedirect = redirect(configure.indexPath, Found)

  def logoutRedirect = redirect(configure.LogoutUrl, Found)

  val authorized: Directive1[String] = {
    println(s"[JP][WARN]: authorized")
    if (configure.Enabled) {
      optionalCookie(configure.CookieName) flatMap {
        case Some(x) => {
          println(s"[JP][WARN]:authorized cookie:$x")
          getSession(x.content) match {
            case Some(cont: String) => provide(cont)
            case None => complete(Unauthorized,"")
          }
        }
        case None =>  complete(Unauthorized,"")
      }
    } else {
      val sessionId = getRandomSessionId
      addSession(sessionId, "*", Long.MaxValue)
      setCookie(HttpCookie(configure.CookieName, sessionId, None, None, None, Option("/")))
      provide("*")
    }
  }

  val secured: Directive1[String] = {
    if (configure.Enabled) {
      println(s"[JP][WARN]: secured")
      optionalCookie(configure.CookieName) flatMap {
        case Some(x) => {
          println(s"[JP][WARN]: secured cookie:$x")
          getSession(x.content) match {
            case Some(cont: String) => provide(cont)
            case None => authorizeRedirect
          }
        }
        case None => authorizeRedirect
      }
    } else {
      val sessionId = getRandomSessionId
      addSession(sessionId, "*", Long.MaxValue )
      setCookie(HttpCookie(configure.CookieName, sessionId, None, None, None, Option("/")))
      provide("*")
    }
  }



  val login = (path("login") & get) {
    println(s"[JP][WARN]: login")
    parameter("code") { code: String =>
      println(s"[JP][WARN]:login code:$code")
      val (token, expires) = getToken(code)
      println(s"[JP][WARN]:login token:$token, expires:$expires")
      val sessionId = getRandomSessionId
      addSession(sessionId, getUserProfile(token),expires*1000)
      setCookie(HttpCookie(configure.CookieName, sessionId, None, Option(expires), None, Option("/"))) {
        indexRedirect
      }
    }
  }

  val logout = path("logout") {
    println(s"[JP][WARN]:logout")
    get {
      optionalCookie(configure.CookieName) {
        case Some(x) => {
          removeSession(x.content)
          deleteCookie(configure.CookieName, path = "/")
          logoutRedirect
        }
        case None => logoutRedirect
      }
    }
  }

  def getToken(code: String): (String, Long) = {
    println(s"[JP][WARN]: getToken:$code")
    val tokenResponse: String = makeGetRq(tokenRq(code))
    val (token: String, expires: Long) = parseTokenRs(tokenResponse)
    (token, expires)
  }

  def getUserProfile(token: String): String = {
    println(s"[JP][WARN]:getUserProfile:$token")
    val profileUrl = s"${configure.ProfileUrl}?access_token=$token"
    makeGetRq(profileUrl)
  }

  def makeGetRq(url: String): String = {
    println(s"[JP][WARN]:makeGetRq:$url")
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val response = pipeline(Get(url))
    val plainResponse: HttpResponse = Await.result(response, Duration.Inf)
    plainResponse.entity.asString
  }


  val secRoute = login ~ logout
}
