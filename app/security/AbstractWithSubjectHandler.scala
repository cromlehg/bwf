package security

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DynamicResourceHandler}
import models.Session
import models.dao.RoleDAO

import scala.concurrent.Future

abstract class AbstractWithSubjectHandler(authSupport: AuthSupport, roleDAO: RoleDAO, dynamicResourceHandler: Option[DynamicResourceHandler] = None)
	extends AbstractHandler(roleDAO, dynamicResourceHandler) {

	import scala.concurrent.Future.{successful => future}

	override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] =
		request.subject match {
			case t: Some[_] => future(t)
			case t => request.session.get(Session.TOKEN) match {
				case Some(token) =>
					val sessionKey = new String(java.util.Base64.getDecoder.decode(token))
					authSupport.getAccount(sessionKey, request.remoteAddress)
				case _ =>
					future(None)
			}
		}


}
