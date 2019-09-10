package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.Permission
import models.dao._
import play.api.Configuration
import play.api.mvc.ControllerComponents
import services.MySQLHelper

import scala.concurrent.ExecutionContext


@Singleton
class DBSController @Inject()(cc: ControllerComponents,
															deadbolt: DeadboltActions,
															config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc, config) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	def adminDatabases = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(MySQLHelper.listDatabases(dbUser, dbPasswd) match {
			case Right(t) => Ok(views.html.admin.systemDatabases(t))
			case Left(msg) => BadRequest(msg)
		})
	}

}

