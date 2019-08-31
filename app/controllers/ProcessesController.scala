package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Permission, SystemProcesses}
import play.api.Configuration
import play.api.mvc.ControllerComponents
import services.{MailVerifier, Mailer}

import scala.concurrent.ExecutionContext


@Singleton
class ProcessesController @Inject()(mailer: Mailer,
																		mailVerifier: MailVerifier,
																		cc: ControllerComponents,
																		deadbolt: DeadboltActions,
																		config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends RegisterCommonAuthorizable(mailer, cc, config) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	def systemProcesses = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.systemProcesses(SystemProcesses.list)))
	}

}

