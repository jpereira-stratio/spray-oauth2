package com.stratio.spray.oauth2.client

import org.scalatest.{FlatSpec, Matchers}

class OauthClientObjectSpec extends FlatSpec with Matchers {
  "parse token" should "parse a queryString" in {
    val (token, expires) = OauthClient.parseTokenRs("token=asdfasdf&expires=123&other=adsfas")
    token should be("asdfasdf")
    expires should be(123L)
  }
  it should "retrieve default values" in {
    val (token, expires) = OauthClient.parseTokenRs("tok0n=asdfasdf&expireD=123&other=adsfas")
    token should be("")
    expires should be(-1L)
  }
  it should "retrieve default values even if the exprire is not a number" in {
    val (token, expires) = OauthClient.parseTokenRs("tok0n=asdfasdf&expires=AAA&other=adsfas")
    token should be("")
    expires should be(-1L)
  }
  "tokenRq" should "retrieve the a valid request" in {
    val url = OauthClient.tokenRq("myToken")
    url should be
    "https://sso.dev.stratio.com:9005/cas/oauth2.0/accessToken?code=myToken&grant_type=authorization_code&" +
      "client_secret=69b17e04246a4fa383f46ec6e28ea1g&" +
      "redirect_uri=http%3A%2F%2Farincon%3A8080%2Flogin&client_id=localhost-client"
  }
  "authorizeRq" should "retrieve a valid authorize request url" in {
    OauthClient.authorizeRq should be
    "https://sso.dev.stratio.com:9005/cas/oauth2.0/authorize?" +
      "redirect_uri=http%3A%2F%2Farincon%3A8080%2Flogin&client_id=localhost-client"
  }

}
