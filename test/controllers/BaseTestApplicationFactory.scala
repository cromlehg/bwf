package controllers

import org.scalatestplus.play.FakeApplicationFactory

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

trait BaseTestApplicationFactory extends FakeApplicationFactory {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure()
      .build()

}
