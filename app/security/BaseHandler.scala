package security

import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DynamicResourceHandler}
import javax.inject.{Inject, Singleton}
import models.Session
import models.dao.RoleDAO
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class BaseHandler @Inject()(authSupport: AuthSupport, roleDAO: RoleDAO)(dynamicResourceHandler: Option[DynamicResourceHandler] = None)
  extends AbstractHandler(roleDAO, dynamicResourceHandler) {

  import scala.concurrent.Future.{successful => future}

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    Future(dynamicResourceHandler.orElse(Some(new BaseDynamicResourceHandler())))

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] =
    request.subject match {
      case Some(subj) =>
        future(Some(subj))
      case _ =>
        request.session.get(Session.TOKEN) match {
          case Some(token) =>
            val sessionKey = new String(java.util.Base64.getDecoder.decode(token))
            authSupport.getAccount(sessionKey, request.remoteAddress)
          case _ =>
            future(None)
        }
    }

}
