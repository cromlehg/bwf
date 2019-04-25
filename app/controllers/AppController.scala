package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import controllers.AuthRequestToAppContext.ac
import models.dao.DAOProvider
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class AppController @Inject()(deadbolt: DeadboltActions,
															cc: ControllerComponents,
															config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends AbstractController(cc)
		with I18nSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def index = deadbolt.WithAuthRequest()() { implicit request =>
		future(Ok(views.html.app.index()))
	}

	def panel = deadbolt.SubjectPresent()() { implicit request =>
		future(Ok(views.html.admin.panel()))
	}

}
