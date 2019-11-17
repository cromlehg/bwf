package controllers

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import models.Account
import models.dao.DAOProvider

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

case class SessionContext(authorizedOpt: Option[models.Account] = None, dap: DAOProvider) {

	import scala.concurrent.Future.{successful => future}

	lazy val actor = authorizedOpt.get

	lazy val afterPageScript: String =
		Await.result(
			dap.options.getOptionByName(models.Options.AFTER_PAGE_SCRIPT).map(_.map(_.value).getOrElse("")),
			5.seconds
		)

	lazy val mainMenu: Option[models.Menu] = {
		val r = dap.options.getOptionByName(models.Options.MAIN_MENU_ID).flatMap {
			case Some(option) =>
				option.toOptInt match {
					case Some(menuId) => dap.menu.getMenuById(menuId)
					case _ => future(None)
				}
			case _ => future(None)
		}
		Await.result(r, 5.seconds)
	}

}

object SessionContextObj {

	def apply(
		subject: Option[Subject],
		dap: DAOProvider
	): SessionContext = SessionContext(
		subject.map(_.asInstanceOf[Account]),
		dap
	)

}

object AuthRequestToSessionContext {

	implicit def sc(
		implicit request: AuthenticatedRequest[_],
		dap: DAOProvider
	) = SessionContextObj(
		request.subject,
		dap,
	)

}
