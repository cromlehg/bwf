package security

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import controllers.AppConstants
import models.dao.RoleDAO
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AbstractHandler(roleDAO: RoleDAO, dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {

  import scala.concurrent.Future.{successful => future}

  override def beforeAuthCheck[A](request: Request[A]) = future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    Future(dynamicResourceHandler.orElse(Some(new BaseDynamicResourceHandler())))

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
    getSubject(request).map { maybeSubject =>
      maybeSubject match {
        case Some(account) =>
          request.headers.get(AppConstants.RETURN_URL)
            .fold {
              Results.Redirect(controllers.routes.AccountsController.denied())
            } { url =>
              Results.Redirect(url).flashing("error" -> "You have no permission!")
            }
        case _ =>
          Results.Redirect(controllers.routes.AccountsController.login)
            .withSession(AppConstants.RETURN_URL -> request.uri)
      }
    }

  override def getPermissionsForRole(roleName: String): Future[List[String]] =
    roleDAO.findPermissionsByRoleName(roleName).map(_.map(_.value).toList).map { permissions =>
      permissions
    }

}