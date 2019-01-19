package security

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import javax.inject.{Inject, Singleton}
import models.dao.RoleDAO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class BaseAccountlessHandler @Inject()(roleDAO: RoleDAO) extends AbstractHandler(roleDAO) {

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] =
    Future(None)

}