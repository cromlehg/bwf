package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.RoleTargetTypes.RoleTargetTypes
import models.dao.RoleDAO
import models.dao.slick.table.RoleTable
import models.{Permission, PermissionTargetTypes, Role}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickRoleDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider,
														 val permissionDAO: SlickPermissionDAO)(implicit ec: ExecutionContext)
	extends RoleDAO with RoleTable with SlickCommonDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryRoleExistsByName = Compiled(
		(name: Rep[String]) => tableRole.filter(_.name.trim.toLowerCase === name.trim.toLowerCase).exists)

	private val queryRoleById = Compiled(
		(id: Rep[Long]) => tableRole.filter(_.id === id))

	def _findRoleById(id: Long) =
		for {
			role <- queryRoleById(id).result.headOption
			permissions <- maybeOptActionSeqR(role)(t => permissionDAO._findPermissionsByTargetId(t.id, PermissionTargetTypes.ROLE).result)
		} yield role.map(_.copy(permissions = permissions))

	def _roleExistsByName(name: String) =
		queryRoleExistsByName(name).result

	def _findRolesByTarget(targetId: Long, targetType: RoleTargetTypes) =
		tableRole
			.filter(_.id in tableRoleToTarget
				.filter(_.targetId === targetId)
				.filter(_.targetType === targetType)
				.map(_.roleId))

	def _findRolesByTargets(targetIds: Seq[Long], targetType: RoleTargetTypes) =
		tableRole
			.filter(_.id in tableRoleToTarget
				.filter(_.targetId inSet targetIds)
				.filter(_.targetType === targetType)
				.map(_.roleId))

	def _findRolesByTargetsWithAssigns(targetIds: Seq[Long], targetType: RoleTargetTypes) =
		tableRoleToTarget
			.filter(_.targetId inSet targetIds)
			.filter(_.targetType === targetType)
			.join(tableRole).on(_.roleId === _.id)

	def _findRoleToTarget(roleId: Long, targetId: Long, targetType: RoleTargetTypes) =
		tableRoleToTarget
			.filter(_.targetId === targetId)
			.filter(_.targetType === targetType)
			.filter(_.roleId === roleId)

	def _findRolesToTargets(roleIds: Seq[Long], targetId: Long, targetType: RoleTargetTypes) =
		tableRoleToTarget
			.filter(_.targetId === targetId)
			.filter(_.targetType === targetType)
			.filter(_.roleId inSet roleIds)

	def _findRolesByTarget(roleNames: Seq[String], targetId: Long, targetType: RoleTargetTypes) =
		tableRole
			.filter(_.name inSet roleNames)
			.filter(_.id in tableRoleToTarget
				.filter(_.targetId === targetId)
				.filter(_.targetType === targetType)
				.map(_.roleId))

	def _findRolesWithPermissionsByTarget(targetId: Long, targetType: RoleTargetTypes) =
		_findRolesByTarget(targetId, targetType).result.flatMap { roles =>
			permissionDAO._findPermissionsByTargetIdsWithAssigns(roles.map(_.id), PermissionTargetTypes.ROLE).result.map { permissionsWithAssigns =>
				roles.map(role => role.copy(permissions = permissionsWithAssigns.filter(_._1._3 == role.id).map(_._2)))
			}
		}

	def _findRolesWithPermissionsByTargetsWithAssigns(targetIds: Seq[Long], targetType: RoleTargetTypes) =
		_findRolesByTargetsWithAssigns(targetIds, targetType).result.flatMap { rolesWithAssigns =>
			permissionDAO._findPermissionsByTargetIdsWithAssigns(rolesWithAssigns.map(_._2.id), PermissionTargetTypes.ROLE).result.map { permissionsWithAssigns =>
				rolesWithAssigns
					.map(roleWithAssign => (roleWithAssign._1._3, roleWithAssign._2.copy(permissions = permissionsWithAssigns.filter(_._1._3 == roleWithAssign._2.id).map(_._2))))
			}
		}

	def _findRoleByName(roleName: String) =
		tableRole.filter(_.name === roleName).result.headOption

	def _findRolesByNames(roleNames: Seq[String]) =
		tableRole.filter(_.name inSet roleNames).result

	def _createRoleToTarget(roleId: Long, targetId: Long, targetType: RoleTargetTypes): DBIOAction[Boolean, NoStream, Effect.Write] =
		(tableRoleToTarget += (roleId, targetType, targetId)).map(_ == 1)

	def _assignRolesToTargetIfNotAssigned(inRoleNames: Seq[String], targetId: Long, targetType: RoleTargetTypes): DBIOAction[Boolean, NoStream, _] = {
		val roleNames: Seq[String] = inRoleNames.distinct
		for {
			alreadyAssignedRoleNames <- _findRolesByTarget(roleNames, targetId, targetType).map(_.name).result
			needsToAssignNames <- DBIO.successful(roleNames.filterNot(alreadyAssignedRoleNames.contains))
			needsToAssignRoles <- _findRolesByNames(needsToAssignNames)
			assigned <-
			if (needsToAssignNames.length == needsToAssignRoles.length) {
				(tableRoleToTarget ++= needsToAssignRoles.map(t => (t.id, targetType, targetId))).map(_ match {
					case Some(count) => count == needsToAssignNames.length
					case _ => false
				})
			} else {
				DBIO.successful(false)
			}
		} yield assigned
	}

	def _assignRoleNameToTargetIfNotAssigned(roleName: String, targetId: Long, targetType: RoleTargetTypes) =
		_findRoleByName(roleName).flatMap(_ match {
			case Some(role) => _assignRoleIdToTargetIfNotAssigned(role.id, targetId, targetType)
			case None => DBIO.successful(false)
		})

	def _assignAndReturnRoleNameToTargetIfNotAssigned(roleName: String, targetId: Long, targetType: RoleTargetTypes) =
		_findRoleByName(roleName).flatMap(_ match {
			case Some(role) => _assignRoleIdToTargetIfNotAssigned(role.id, targetId, targetType).map(_ => Some(role))
			case None => DBIO.successful(None)
		})

	def _assignRoleIdToTargetIfNotAssigned(roleId: Long, targetId: Long, targetType: RoleTargetTypes) =
		_findRoleToTarget(roleId, targetId, targetType).result.headOption.flatMap(_ match {
			case Some(_) => DBIO.successful(true)
			case _ => _createRoleToTarget(roleId, targetId, targetType)
		})

	def _findPermissionsByRoleName(roleName: String) =
		_findRoleByName(roleName).flatMap(_ match {
			case Some(role) => permissionDAO._findPermissionsByTargetId(role.id, PermissionTargetTypes.ROLE).result
			case _ => DBIO.successful(Seq.empty[Permission])
		})

	def _rolesListPage(pageSize: Int, pageId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) =
		tableRole
			.filterOpt(filterOpt) { case (t, filter) => t.name.like("%" + filter.trim + "%") || t.descr.like("%" + filter.trim + "%") }
			.dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
			.page(pageSize, pageId)

	def _rolesListPagesCount(pSize: Int, filterOpt: Option[String]) =
		tableRole
			.filterOpt(filterOpt) { case (t, filter) => t.name.like("%" + filter.trim + "%") || t.descr.like("%" + filter.trim + "%") }
			.size

	def _createRole(name: String, descr: Option[String]) =
		tableRole returning tableRole.map(_.id) into ((v, id) => v.copy(id = id)) += models.Role(
			0,
			name.trim.toLowerCase,
			descr)

	def _updateRole(id: Long, name: String, descr: Option[String]) =
		tableRole
			.filter(_.id === id)
			.map(t => (t.name, t.descr))
			.update(name.trim.toLowerCase, descr)
			.map(_ == 1)

	override def findRoleById(id: Long): Future[Option[Role]] =
		db.run(_findRoleById(id))

	override def createRole(name: String, descr: Option[String]): Future[Role] =
		db.run(_createRole(name, descr).transactionally)

	override def updateRole(id: Long, name: String, descr: Option[String]): Future[Boolean] =
		db.run(_updateRole(id, name, descr).transactionally)

	override def roleExistsByName(name: String): Future[Boolean] =
		db.run(_roleExistsByName(name))

	override def rolesListPage(pageSize: Int, pageId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Role]] =
		db.run(_rolesListPage(pageSize, pageId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

	override def rolesListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
		db.run(_rolesListPagesCount(pSize, filterOpt).result).map(pages(_, pSize))

	override def findPermissionsByRoleName(roleName: String): Future[Seq[Permission]] =
		db.run(_findPermissionsByRoleName(roleName))

	override def findRolesByTarget(targetId: Long, targetType: RoleTargetTypes): Future[Seq[Role]] =
		db.run(_findRolesByTarget(targetId, targetType).result)

	override def assignRoleToTargetIfNotAssigned(roleId: Long, targetId: Long, targetType: RoleTargetTypes): Future[Boolean] =
		db.run(_assignRoleIdToTargetIfNotAssigned(roleId, targetId, targetType).transactionally)

	override def assignRoleToTargetIfNotAssigned(roleName: String, targetId: Long, targetType: RoleTargetTypes): Future[Boolean] =
		db.run(_assignRoleNameToTargetIfNotAssigned(roleName, targetId, targetType).transactionally)

	override def assignRolesToTargetIfNotAssigned(roleNames: Seq[String], targetId: Long, targetType: RoleTargetTypes): Future[Boolean] =
		db.run(_assignRolesToTargetIfNotAssigned(roleNames, targetId, targetType).transactionally)

	override def close: Future[Unit] =
		future(db.close())

}
