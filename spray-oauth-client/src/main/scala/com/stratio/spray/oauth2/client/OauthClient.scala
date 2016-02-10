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

package com.stratio.spray.oauth2.client

import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import org.apache.oltu.oauth2.common.message.types.GrantType
import spray.client.pipelining._
import spray.http.StatusCodes._
import spray.http._
import spray.routing._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

trait OauthClient extends HttpService with SessionStore {

  implicit val ec: ExecutionContext = ExecutionContext.global

  import Config._
  import OauthClient._

  def authorizeRedirect = redirect(authorizeRq, Found)

  def indexRedirect = redirect(indexPath, Found)

  def logoutRedirect = redirect(LogoutUrl, Found)

  val secured: Directive1[String] = {
    optionalCookie(CookieName) flatMap {
      case Some(x) => {
        getSession(x.content) match {
          case Some(cont: String) => provide(cont)
          case None => authorizeRedirect
        }
      }
      case None => authorizeRedirect
    }
  }

  val login = (path("login") & get) {
    parameter("code") { code: String =>
      val (token, expires) = getToken(code)
      val sessionId = getRandomSessionId
      addSession(sessionId, getUserProfile(token))
      setCookie(HttpCookie(CookieName, sessionId, None, Option(expires), None, Option("/"))) {
        indexRedirect
      }
    }
  }
  val logout = path("logout") {
    get {
      optionalCookie(CookieName) {
        case Some(x) => {
          removeSession(x.content)
          deleteCookie(CookieName, path = "/")
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
    val profileUrl = s"$ProfileUrl?access_token=$token"
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

object OauthClient {
  import Config._
  val DefaultExpiration: String = "-1"

  val authorizeRq: String = OAuthClientRequest
    .authorizationLocation(AuthorizeUrl)
    .setClientId(ClientId)
    .setRedirectURI(RedirectUrl)
    .buildQueryMessage().getLocationUri

  def parseTokenRs(tokenResponse: String): (String, Long) = {
    val r = "((\\w+)=(\\w+))+".r
    val parsedMap = r.findAllIn(tokenResponse).matchData.
      map(g => Map(g.subgroups(1) -> g.subgroups(2))).foldLeft(Map[String, String]())(_ ++ _)

    val token = parsedMap.getOrElse("token", "")

    val expires: Long = parseExpires(parsedMap)
    (token, expires)
  }

  private def parseExpires(parsedMap: Map[String, String]): Long = {
    Try {
      parsedMap.getOrElse("expires", DefaultExpiration).toLong
    }.toOption match {
      case Some(x: Long) => x
      case None => DefaultExpiration.toLong
    }
  }

  def tokenRq(code: String): String = {
    OAuthClientRequest.tokenLocation(accessTokenUrl)
      .setGrantType(GrantType.AUTHORIZATION_CODE)
      .setClientId(ClientId)
      .setClientSecret(ClientSecret)
      .setRedirectURI(RedirectUrl)
      .setCode(code)
      .buildQueryMessage.getLocationUri
  }
}
