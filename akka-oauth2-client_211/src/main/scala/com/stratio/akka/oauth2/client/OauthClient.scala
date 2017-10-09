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

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.directives.ParameterDirectives.ParamMagnet
import akka.http.scaladsl.server.{Directive1, Directives}
import akka.stream.ActorMaterializer
import com.stratio.akka.oauth2.client.SessionStore._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


trait OauthClient extends Directives{
  implicit val ec: ExecutionContext = ExecutionContext.global

  val configure = new Config

  import OauthClientHelper._

  def authorizeRedirect = redirect(authorizeRq, Found)

  def indexRedirect = redirect(configure.indexPath, StatusCodes.Found)

  def logoutRedirect = redirect(configure.LogoutUrl, StatusCodes.Found)

  val authorized: Directive1[String] = {
    if (configure.Enabled) {
      optionalCookie(configure.CookieName) flatMap {
        case Some(x) => {
          getSession(x.toCookie().value) match {
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
          getSession(x.toCookie().value) match {
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
    parameter(ParamMagnet.apply("code")) { code: String =>
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
          removeSession(x.toCookie().value)
          deleteCookie(configure.CookieName, path = "/")
          logoutRedirect
        }
        case None => logoutRedirect
      }
    }
  }

  val isLogged = path("isLogged") {
    get {
      optionalCookie(configure.CookieName) {
        case Some(x) => {
          getSession(x.toCookie().value) match{
            case Some(identity) => complete(identity)
            case None => indexRedirect
          }
        }
        case None => indexRedirect
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

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def makeGetRq(url: String): String = {
    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = url))
    val plainResponse: HttpResponse = Await.result(responseFuture, Duration.Inf)
    plainResponse.entity.toString
  }

}
