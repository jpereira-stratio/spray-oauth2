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

import com.typesafe.config.ConfigFactory

object Config {

  val conf = ConfigFactory.load().getConfig("oauth2")
  val AuthorizeUrl: String = conf.getString("url.authorize")
  val accessTokenUrl: String = conf.getString("url.accessToken")
  val ProfileUrl: String = conf.getString("url.profile")
  val RedirectUrl: String = conf.getString("url.callBack")
  val LogoutUrl: String = conf.getString("url.logout")
  val indexPath: String = conf.getString("url.onLoginGoTo")
  val ClientId: String = conf.getString("client.id")
  val ClientSecret: String = conf.getString("client.secret")
  val CookieName: String = conf.getString("cookieName")
}
