package security

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import javax.inject.{Inject, Singleton}
import models.dao.RoleDAO
import play.api.mvc.{Result, Results}

import scala.concurrent.Future

@Singleton
class BaseAccountlessHandler @Inject()(roleDAO: RoleDAO) extends AbstractHandler(roleDAO) {

	import scala.concurrent.Future.{successful => future}

	override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] =
		future(None)

	override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
		future(Results.Redirect(controllers.routes.AccountsController.denied()))


}
