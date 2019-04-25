package models.dao

import scala.concurrent.Future
import javax.inject.Inject
import models.RoleTargetTypes.RoleTargetTypes
import play.api.inject.ApplicationLifecycle
import models.{Permission, Role}

trait RoleDAO {

  def findRolesByTarget(targetId: Long, targetType: RoleTargetTypes): Future[Seq[Role]]

  def findPermissionsByRoleName(roleName: String): Future[Seq[Permission]]

  def assignRoleToTargetIfNotAssigned(roleId: Long, targetId: Long, targetType: RoleTargetTypes): Future[Boolean]

  def assignRoleToTargetIfNotAssigned(roleName: String, targetId: Long, targetType: RoleTargetTypes): Future[Boolean]

  def assignRolesToTargetIfNotAssigned(roleNames: Seq[String], targetId: Long, targetType: RoleTargetTypes): Future[Boolean]

  def rolesListPage(pageSize: Int, pageId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Role]]

  def rolesListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

	def roleExistsByName(name: String): Future[Boolean]

	def createRole(name: String, descr: Option[String]): Future[Role]

	def findRoleById(id: Long): Future[Option[Role]]

	def updateRole(id: Long, name: String, descr: Option[String]): Future[Boolean]

  def close: Future[Unit]

}

class RoleDAOCloseHook @Inject() (dao: RoleDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
