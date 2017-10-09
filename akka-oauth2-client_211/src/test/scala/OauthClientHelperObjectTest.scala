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
import com.stratio.akka.oauth2.client.{Config, OauthClientHelper}
import com.typesafe.config.ConfigValueFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class OauthClientHelperObjectTest extends FlatSpec with Matchers {

  val user: String =
    """{"id":"sparkta1","attributes":[{"cn":"manager_2"},
      |{"my_role_name":["sparkta_designer","sparkta_manager","sparkta_executor"]}]}""".stripMargin

  "parse token" should "parse a queryString" in {
    val (token, expires) = OauthClientHelper.parseTokenRs("access_token=asdf-asdf&expires=7200")
    token should be("asdf-asdf")
    expires should be(7200L)
  }
  it should "retrieve default values" in {
    val (token, expires) = OauthClientHelper.parseTokenRs("tok0n=asdfasdf&expireD=123&other=adsfas")
    token should be("")
    expires should be(-1L)
  }
  it should "retrieve default values even if the exprire is not a number" in {
    val (token, expires) = OauthClientHelper.parseTokenRs("tok0n=asdfasdf&expires=AAA&other=adsfas")
    token should be("")
    expires should be(-1L)
  }
  "tokenRq" should "retrieve the a valid request" in {
    val url = OauthClientHelper.tokenRq("myToken")
    url should be
    "https://accounts.google.com/o/oauth2/token?code=myToken&grant_type=authorization_code&" +
      "client_secret=69b17e04246a4fa383f46ec6e28ea1g&" +
      "redirect_uri=http%3A%2F%2Farincon%3A8080%2Flogin&client_id=localhost-client"
  }
  "authorizeRq" should "retrieve a valid authorize request url" in {
    OauthClientHelper.authorizeRq should be
    "https://accounts.google.com/o/oauth2/auth?" +
      "redirect_uri=http%3A%2F%2Farincon%3A8080%2Flogin&client_id=localhost-client"
  }
  "Has Roles" should "return true if role is wildcard" in {
    OauthClientHelper.hasRole(Seq("*"), user) should be (true)
  }
  it should "return true if role is sparkta_designer and other" in {
    OauthClientHelper.hasRole(Seq("sparkta_designer","other"), user) should be (true)
  }
  it should "return false if role  other" in {
    OauthClientHelper.hasRole(Seq("other"), user) should be (false)
  }
  "with enabled false" should "return true"in{
    val conf=new Config
    val c=conf.configuration.withValue("enable",ConfigValueFactory.fromAnyRef("false"))
    val otherConf=new Config(c)

    OauthClientHelper.hasRole(Seq("other"), user,otherConf) should be (true)
  }
}
