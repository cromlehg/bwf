package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import play.api.db.Database

class AppControllerSpec() extends PlaySpec with GuiceOneAppPerTest with Injecting with BaseTestApplicationFactory {

  "AppController" should {

    "render the index page" in {
      val result = route(app, FakeRequest(GET, "/")).get
      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
      contentAsString(result) must include(AppConstants.APP_NAME)
    }

  }

}
