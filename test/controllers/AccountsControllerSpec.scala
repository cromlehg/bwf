package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.db.Database

class AccountsControllerSpec() extends PlaySpec with GuiceOneAppPerTest with Injecting with BaseTestApplicationFactory {

  "AccountsController" should {

    "render the login page" in {
      val result = route(app, FakeRequest(GET, "/app/login")).get
      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include("login")
      contentAsString(result) must include("pass")
    }

    "wrong login or password" in {
      val result = route(app, FakeRequest(POST, "/app/login")
        .withFormUrlEncodedBody(
          "email" -> "wronglogin@wrongproject.wronjcoutry",
          "pass" -> "badPassword123456789")).get

      status(result) mustBe BAD_REQUEST

      // FIXME: Why not works?
      //flash(result).get("error") mustBe Some(message("app.login.error"))
      contentType(result) mustBe Some("text/html")

      contentAsString(result) must include("login")
      contentAsString(result) must include("pass")
    }

    "login success" in {
      val result = route(app, FakeRequest(POST, "/app/login")
        .withFormUrlEncodedBody(
          "email" -> TestConstants.adminEmail,
          "pass" -> TestConstants.adminPass)).get

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some("/")

      val redirectResult = route(
        app,
        FakeRequest(GET, redirectLocation(result).get)
          .withSession(session(result).data.toMap.toSeq: _*)
          .withCookies(cookies(result).toSeq: _*)).get

      status(redirectResult) mustBe OK
      contentType(redirectResult) mustBe Some("text/html")
      contentAsString(redirectResult) must include("logout")
    }

    "clients have no access to options" in {
      val result = route(app, FakeRequest(POST, "/app/login")
        .withFormUrlEncodedBody(
          "email" -> TestConstants.clientEmail,
          "pass" -> TestConstants.clientPass)).get

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some("/")

      val redirectResult = route(
        app,
        FakeRequest(GET, redirectLocation(result).get)
          .withSession(session(result).data.toMap.toSeq: _*)
          .withCookies(cookies(result).toSeq: _*)).get

      status(redirectResult) mustBe OK
      contentType(redirectResult) mustBe Some("text/html")
      contentAsString(redirectResult) must include("logout")

      val optionsResult = route(
        app,
        FakeRequest(GET, "/app/admin/options")
          .withSession(session(result).data.toMap.toSeq: _*)
          .withCookies(cookies(result).toSeq: _*)).get

      status(optionsResult) mustBe SEE_OTHER
      redirectLocation(optionsResult) mustBe Some("/app/denied")

      val redirectResultAfter = route(
        app,
        FakeRequest(GET, redirectLocation(optionsResult).get)
          .withSession(session(optionsResult).data.toMap.toSeq: _*)
          .withCookies(cookies(optionsResult).toSeq: _*)).get

      status(redirectResultAfter) mustBe FORBIDDEN
      contentType(redirectResultAfter) mustBe Some("text/html")
      contentAsString(redirectResultAfter) must include("Access denied")
    }

    "admin have access to options" in {
      val result = route(app, FakeRequest(POST, "/app/login")
        .withFormUrlEncodedBody(
          "email" -> TestConstants.adminEmail,
          "pass" -> TestConstants.adminPass)).get

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some("/")

      val redirectResult = route(
        app,
        FakeRequest(GET, redirectLocation(result).get)
          .withSession(session(result).data.toMap.toSeq: _*)
          .withCookies(cookies(result).toSeq: _*)).get

      status(redirectResult) mustBe OK
      contentType(redirectResult) mustBe Some("text/html")
      contentAsString(redirectResult) must include("logout")

      val optionsResult = route(
        app,
        FakeRequest(GET, "/app/admin/options")
          .withSession(session(result).data.toMap.toSeq: _*)
          .withCookies(cookies(result).toSeq: _*)).get

      status(optionsResult) mustBe OK
      contentType(optionsResult) mustBe Some("text/html")
      //contentAsString(optionsResult) must include("Access denied")
    }

  }

}
