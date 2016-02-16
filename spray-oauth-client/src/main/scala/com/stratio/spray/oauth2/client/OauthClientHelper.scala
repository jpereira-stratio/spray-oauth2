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

import scala.util.Try
import scala.util.parsing.json.JSON


object OauthClientHelper {

  val conf = new Config


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

  def gerRoles(user: String): Seq[Seq[String]] = {
    val parsed = JSON.parseFull(user).get.asInstanceOf[Map[String, Any]]
    val attrib = parsed.get("attributes").get.asInstanceOf[Seq[Map[String, Any]]]
    attrib.filter {
      _.contains("ROLE")
    }.flatten.map(_ match {
      case (role: String, roles: Any) => roles
    }).asInstanceOf[Seq[Seq[String]]]
  }

  def hasRole(role: Seq[String], user: String, conf: Config = conf): Boolean = {
    if (conf.Enabled) {
      val roles: Seq[Seq[String]] = gerRoles(user)
      val result: Boolean = role.map(r => roles.contains(r)).foldLeft(false)(_ || _)
      role match {
        case Seq("*") => true
        case _ =>roles.flatMap(v =>
          v.map(v =>
            role.contains(v))
        ).contains(true)
      }
    } else true
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
