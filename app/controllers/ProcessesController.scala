package controllers

import java.io.{BufferedReader, InputStreamReader}

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.models.PatternType
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{ConfirmationStatus, Permission, Role, Session, SystemProcess}
import org.mindrot.jbcrypt.BCrypt
import play.Logger
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{boolean, email, mapping, nonEmptyText}
import play.api.i18n.Messages
import play.api.mvc.{ControllerComponents, Flash, Request, Result}
import play.twirl.api.Html
import services.{MailVerifier, Mailer, MailerResponse}
import ua.parser.Parser

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random
import scala.util.matching.Regex


@Singleton
class ProcessesController @Inject()(mailer: Mailer,
																		mailVerifier: MailVerifier,
																		cc: ControllerComponents,
																		deadbolt: DeadboltActions,
																		config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends RegisterCommonAuthorizable(mailer, cc, config) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	val pFEWPattern = """(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*)""".r

	def processes = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		val p = Runtime.getRuntime().exec("ps -few");
		val input = new BufferedReader(new InputStreamReader(p.getInputStream()))
		val processesStrs = Stream.continually(input.readLine()).takeWhile(_ != null).drop(1)
		val processes = processesStrs.map(_ match {
			case pFEWPattern(uid, pid, ppid, c, stime, tty, time, cmd) =>
				Some(SystemProcess(uid, pid, ppid, c, stime, tty, time, cmd))
			case _ => None
		}).flatten

		future(Ok(views.html.admin.processes(processes)))
	}

}

