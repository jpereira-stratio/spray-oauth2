/*
 * © 2017 Stratio Big Data Inc., Sucursal en España.
 *
 * This software is licensed under the Apache License 2.0. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the terms of the License for more details.
 *
 * SPDX-License-Identifier: Apache-2.0.
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

  def getRoles(user: String): Seq[Seq[String]] = {
    val parsed = JSON.parseFull(user).get.asInstanceOf[Map[String, Any]]
    val attrib = parsed.get("attributes").get.asInstanceOf[Seq[Map[String, Any]]]
    attrib.filter {
      _.contains(conf.RoleName)
    }.flatten.map(_ match {
      case (role: String, roles: Any) => roles
      case (x)=>throw new RuntimeException("the user has no roles")
    }).asInstanceOf[Seq[Seq[String]]]
  }

  def hasRole(role: Seq[String], user: String, conf: Config = conf): Boolean = {
    if (conf.Enabled) {
      val roles: Seq[Seq[String]] = getRoles(user)
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
