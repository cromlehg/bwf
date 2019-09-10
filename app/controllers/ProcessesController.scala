package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Permission, SystemProcesses}
import play.api.Configuration
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext


@Singleton
class ProcessesController @Inject()(cc: ControllerComponents,
																		deadbolt: DeadboltActions,
																		config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc, config) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	def systemProcesses = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
    SystemProcesses.list.map(_ match {
      case Right(t) => Ok(views.html.admin.systemProcesses(t))
      case Left(pe) => BadRequest(pe.descr.getOrElse("Error"))
    })
	}

}

