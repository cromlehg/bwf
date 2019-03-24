package models.dao

import javax.inject.Inject
import models.{Permission, Role}
import models.PermissionTargetTypes.PermissionTargetTypes
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait PermissionDAO {

	def findPermissionsByTargetId(targetId: Long, targetType: PermissionTargetTypes): Future[Seq[Permission]]

	def assignPermissionToTargetIfNotAssigned(permissionId: Long, targetId: Long, targetType: PermissionTargetTypes): Future[Boolean]

	def assignPermissionsToTargetIfNotAssigned(permissionIds: Seq[Long], targetId: Long, targetType: PermissionTargetTypes): Future[Int]

	def removePermissionFromTarget(permissionId: Long, targetId: Long, targetType: PermissionTargetTypes): Future[Int]

	def removePermissionsFromTarget(permissionIds: Seq[Long], targetId: Long, targetType: PermissionTargetTypes): Future[Int]

	def permissionsList: Future[Seq[Permission]]

	def permissionsListPage(pageSize: Int, pageId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Permission]]

	def permissionsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

	def permissionsListPagesCountByTarget(targetId: Long,
																				targetType: PermissionTargetTypes,
																				pSize: Int,
																				filterOpt: Option[String]): Future[Int]

	def permissionsListPageByTarget(targetId: Long,
																	targetType: PermissionTargetTypes,
																	pageSize: Int,
																	pageId: Int,
																	sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Permission]]

	def permissionExistsByValue(value: String): Future[Boolean]

	def createPermission(value: String, descr: Option[String]): Future[Permission]

	def updatePermission(id: Long, value: String, descr: Option[String]): Future[Boolean]

	def findPermissionById(id: Long): Future[Option[Permission]]

	def close: Future[Unit]

}

class PermissionDAOCloseHook @Inject()(dao: PermissionDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
