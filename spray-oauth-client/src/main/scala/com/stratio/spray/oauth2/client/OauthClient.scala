/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
      addSession(sessionId, "*")
      setCookie(HttpCookie(configure.CookieName, sessionId, None, None, None, Option("/")))
      provide("*")
    }
  }

  val login = (path("login") & get) {
    parameter("code") { code: String =>
      val (token, expires) = getToken(code)
      val sessionId = getRandomSessionId
      addSession(sessionId, getUserProfile(token))
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
