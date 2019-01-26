package security

import be.objectify.deadbolt.scala.AuthenticatedRequest
import javax.inject.{Inject, Singleton}
import models.dao.RoleDAO
import play.api.mvc.{Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class JSONBasedHandler @Inject()(authSupport: AuthSupport, roleDAO: RoleDAO) extends AbstractWithSubjectHandler(authSupport, roleDAO) {

	override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
		getSubject(request).map(_.fold(Results.Unauthorized) { _ => Results.Forbidden })

}
