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
package com.stratio.akka.oauth2.client

import com.typesafe.config.ConfigFactory

import scala.util.Try

class Config(conf: com.typesafe.config.Config = ConfigFactory.load().getConfig("oauth2")) {

  val AuthorizeUrl: String = getDefaultString("url.authorize", Option(""))
  val accessTokenUrl: String = getDefaultString("url.accessToken", Option(""))
  val ProfileUrl: String = getDefaultString("url.profile", Option(""))
  val RedirectUrl: String = getDefaultString("url.callBack", Option(""))
  val LogoutUrl: String = getDefaultString("url.logout", Option(""))
  val indexPath: String = getDefaultString("url.onLoginGoTo", Option("/"))
  val ClientId: String = getDefaultString("client.id", Option(""))
  val ClientSecret: String = getDefaultString("client.secret", Option(""))
  val CookieName: String = getDefaultString("cookieName", Option("user"))
  val Enabled: Boolean = getDefaultBoolean("enable", false)
  val RoleName: String = getDefaultString("roleName",Option("roles"))
  val configuration = conf

  private def getDefaultBoolean(key: String, defaultValue: Boolean): Boolean = {
    Try(conf.getString(key).toBoolean).toOption match {
      case Some(x) => x
      case None => defaultValue
    }
  }

  private def getDefaultString(key: String, defaultValue: Option[String]): String = {
    val optionValue = Try(conf.getString(key)).toOption match {
      case Some(x) => Option(x)
      case None => defaultValue
    }
    optionValue.fold(throw new RuntimeException(s"$key is not defned"))(x => x)

  }

}
