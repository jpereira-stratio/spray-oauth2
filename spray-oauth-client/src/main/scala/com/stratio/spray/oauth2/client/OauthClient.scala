/*
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache License 2.0. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the terms of the License for more details.
 *
 * SPDX-License-Identifier: Apache-2.0.
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
    if (configure.Enabled) {
      optionalCookie(configure.CookieName) flatMap {
        case Some(x) => {
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
      optionalCookie(configure.CookieName) flatMap {
        case Some(x) => {
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
    parameter("code") { code: String =>
      val (token, expires) = getToken(code)
      val sessionId = getRandomSessionId
      addSession(sessionId, getUserProfile(token),expires*1000)
      setCookie(HttpCookie(configure.CookieName, sessionId, None, Option(expires), None, Option("/"))) {
        indexRedirect
      }
    }
  }

  val logout = path("logout") {
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
    val tokenResponse: String = makeGetRq(tokenRq(code))
    val (token: String, expires: Long) = parseTokenRs(tokenResponse)
    (token, expires)
  }

  def getUserProfile(token: String): String = {
    val profileUrl = s"${configure.ProfileUrl}?access_token=$token"
    makeGetRq(profileUrl)
  }

  def makeGetRq(url: String): String = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    val response = pipeline(Get(url))
    val plainResponse: HttpResponse = Await.result(response, Duration.Inf)
    plainResponse.entity.asString
  }


  val secRoute = login ~ logout
}
