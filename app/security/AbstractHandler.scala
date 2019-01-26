package security

import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import models.dao.RoleDAO
import play.api.mvc.Request

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AbstractHandler(roleDAO: RoleDAO, dynamicResourceHandler: Option[DynamicResourceHandler] = None) extends DeadboltHandler {

	import scala.concurrent.Future.{successful => future}

  override def beforeAuthCheck[A](request: Request[A]) = future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    Future(dynamicResourceHandler.orElse(Some(new BaseDynamicResourceHandler())))

	override def getPermissionsForRole(roleName: String): Future[List[String]] =
    roleDAO.findPermissionsByRoleName(roleName).map(_.map(_.value).toList).map { permissions =>
      permissions
    }

}
