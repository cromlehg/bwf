package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import models.dao._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class TagController @Inject()(
  cc: ControllerComponents,
  config: Configuration,
  deadbolt: DeadboltActions,
  tagDAO: TagDAO
)(implicit ec: ExecutionContext)
  extends AbstractController(cc)
  with I18nSupport
  with LoggerSupport {

  def search(query: String) = deadbolt.WithAuthRequest()() { implicit request =>
    tagDAO.search(query) map { tags =>
      val json = Json.toJson(tags.map(_.name))
      Ok(json)
    }
  }

}
