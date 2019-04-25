package controllers

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import models.Account
import models.dao.DAOProvider

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

case class AppContext(val authorizedOpt: Option[models.Account] = None, dap: DAOProvider) {

	lazy val actor = authorizedOpt.get

	lazy val afterPageScript: String =
		Await.result(dap.options
			.getOptionByName(models.Options.AFTER_PAGE_SCRIPT)
			.map(_.map(_.value).getOrElse("")), 5.seconds)

}

object AppContextObj {

	def apply(subject: Option[Subject],
						dap: DAOProvider): AppContext =
		new AppContext(
			subject.map(_.asInstanceOf[Account]),
			dap)

}

object AuthRequestToAppContext {

	implicit def ac(implicit
									request: AuthenticatedRequest[_],
									dap: DAOProvider) =
		AppContextObj(
			request.subject,
			dap)

}
