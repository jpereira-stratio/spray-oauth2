/*
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

import org.apache.oltu.oauth2.client.request.OAuthClientRequest
import org.apache.oltu.oauth2.common.message.types.GrantType
import org.slf4j.LoggerFactory

import scala.util.Try
import scala.util.parsing.json.JSON


object OauthClientHelper {

  val conf = new Config
  private lazy val log = LoggerFactory.getLogger(this.getClass)

  val DefaultExpiration: String = "-1"

  val authorizeRq: String = OAuthClientRequest
    .authorizationLocation(conf.AuthorizeUrl)
    .setClientId(conf.ClientId)
    .setRedirectURI(conf.RedirectUrl)
    .buildQueryMessage().getLocationUri

  def parseTokenRs(tokenResponse: String): (String, Long) = {
    val r = "([^?=&]+)(=([^&]*))?".r
    val parsedMap = r.findAllIn(tokenResponse).matchData.
      map(g => Map(g.subgroups(0) -> g.subgroups(2))).
      foldLeft(Map[String, String]())(_ ++ _)

    val token = parsedMap.getOrElse("access_token", "")

    val expires: Long = parseExpires(parsedMap)
    (token, expires)
  }

  def getRoles(user: String): Seq[Seq[String]] = {
    log.debug(s"Getting roles for user: [$user]. Role Name property:[${conf.RoleName}]")
    val parsed = JSON.parseFull(user).get.asInstanceOf[Map[String, Any]]
    val attrib = parsed.get("attributes").get.asInstanceOf[Seq[Map[String, Any]]]
    log.debug(s"User attributes: [$attrib]")

    attrib.filter {
      _.contains(conf.RoleName)
    }.flatten.map(_ match {
      case (role: String, roles: Any) => {
        log.debug(s"Found roles:[$roles]; role property:[$role]")
        roles
      }
      case (x)=>throw new RuntimeException("the user has no roles")
    }).asInstanceOf[Seq[Seq[String]]]
  }

  def hasRole(role: Seq[String], user: String, conf: Config = conf): Boolean = {
    if (conf.Enabled) {
      log.debug(s"Checking if user [$user] has permitted role: [$role]")
      val roles: Seq[Seq[String]] = getRoles(user)
      log.debug(s"User [$user] roles:[$roles]")
      val auth = role match {
        case Seq("*") => true
        case _ =>roles.flatMap(v =>
          v.map(v =>
            role.contains(v))
        ).contains(true)
      }
      if (auth){
        log.debug(s"User [$user] belongs to permitted roles")
      }else{
        log.info(s"User [$user] does not contains required roles. Please check permitted roles list [$role]")
      }
      auth
    } else {
      log.debug(s"security disabled. Avoiding role checking")
      true
    }
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
    OAuthClientRequest.tokenLocation(conf.accessTokenUrl)
      .setGrantType(GrantType.AUTHORIZATION_CODE)
      .setClientId(conf.ClientId)
      .setClientSecret(conf.ClientSecret)
      .setRedirectURI(conf.RedirectUrl)
      .setCode(code)
      .buildQueryMessage.getLocationUri
  }
}
