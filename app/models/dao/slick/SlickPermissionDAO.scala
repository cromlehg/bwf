package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.Permission
import models.PermissionTargetTypes.PermissionTargetTypes
import models.dao.PermissionDAO
import models.dao.slick.table.PermissionTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

@Singleton
class SlickPermissionDAO @Inject()(
																		val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends PermissionDAO with PermissionTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryPermissionExistsByValue = Compiled(
		(value: Rep[String]) => tablePermission.filter(_.value.trim.toLowerCase === value.trim.toLowerCase).exists)

	private val queryPermissionById = Compiled(
		(id: Rep[Long]) => tablePermission.filter(_.id === id))

	def _findPermissionById(id: Long) =
		queryPermissionById(id).result.headOption

	def _permissionExistsByValue(value: String) =
		queryPermissionExistsByValue(value).result

	def _findPermissionsByTargetId(targetId: Long, targetType: PermissionTargetTypes) =
		tablePermission
			.filter(_.id in tablePermissionToTarget
				.filter(_.targetId === targetId)
				.filter(_.targetType === targetType)
				.map(_.permissionId))

	def _findPermissionsByTargetIds(targetIds: Seq[Long], targetType: PermissionTargetTypes) =
		tablePermission
			.filter(_.id in tablePermissionToTarget
				.filter(_.targetId inSet targetIds)
				.filter(_.targetType === targetType)
				.map(_.permissionId))

	def _findPermissionsByTargetIdsWithAssigns(targetIds: Seq[Long], targetType: PermissionTargetTypes) =
		tablePermissionToTarget
			.filter(_.targetId inSet targetIds)
			.filter(_.targetType === targetType)
			.join(tablePermission).on(_.permissionId === _.id)

	def _findPermissionToTarget(permissionId: Long, targetId: Long, targetType: PermissionTargetTypes) =
		tablePermissionToTarget
			.filter(_.targetId === targetId)
			.filter(_.targetType === targetType)
			.filter(_.permissionId === permissionId)

	def _createPermissionToTarget(permissionId: Long, targetId: Long, targetType: PermissionTargetTypes): DBIOAction[Boolean, NoStream, Effect.Write] =
		(tablePermissionToTarget += (permissionId, targetType, targetId)).map(_ == 1)

	def _assignPermissionToTargetIfNotAssigned(permissionId: Long, targetId: Long, targetType: PermissionTargetTypes) =
		_findPermissionToTarget(permissionId, targetId, targetType).result.headOption.flatMap(_ match {
			case Some(_) => DBIO.successful(false)
			case _ => _createPermissionToTarget(permissionId, targetId, targetType)
		})

	def _permissionsListPage(pageSize: Int, pageId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) =
		tablePermission
			.filterOpt(filterOpt) { case (t, filter) => t.value.like("%" + filter.trim + "%") || t.descr.like("%" + filter.trim + "%") }
			.dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
			.page(pageSize, pageId)

	def _permissionsListPagesCount(pSize: Int, filterOpt: Option[String]) =
		tablePermission
			.filterOpt(filterOpt) { case (t, filter) => t.value.like("%" + filter.trim + "%") || t.descr.like("%" + filter.trim + "%") }
			.size


	implicit class permissionsFilterByTarget[T[_]](query: Query[SlickPermissionDAO.this.InnerCommonTablePermission, SlickPermissionDAO.this.InnerCommonTablePermission#TableElementType, T]) {
		def filterByTarget(targetId: Long, targetType: PermissionTargetTypes): Query[SlickPermissionDAO.this.InnerCommonTablePermission, SlickPermissionDAO.this.InnerCommonTablePermission#TableElementType, T] =
			query.filter(_.id in tablePermissionToTarget
				.filter(_.targetId === targetId)
				.filter(_.targetType === targetType)
				.map(_.permissionId))
	}

	def _permissionsListPageByTarget(targetId: Long, targetType: PermissionTargetTypes, pageSize: Int, pageId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) =
		tablePermission
			.filterByTarget(targetId, targetType)
			.filterOpt(filterOpt) { case (t, filter) => t.value.like("%" + filter.trim + "%") || t.descr.like("%" + filter.trim + "%") }
			.dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
			.page(pageSize, pageId)

	def _permissionsListPagesCountByTarget(targetId: Long, targetType: PermissionTargetTypes, pSize: Int, filterOpt: Option[String]) =
		tablePermission
			.filterByTarget(targetId, targetType)
			.filterOpt(filterOpt) { case (t, filter) => t.value.like("%" + filter.trim + "%") || t.descr.like("%" + filter.trim + "%") }
			.size

	def _createPermission(value: String, descr: Option[String]) =
		tablePermission returning tablePermission.map(_.id) into ((v, id) => v.copy(id = id)) += models.Permission(
			0,
			value.trim.toLowerCase,
			descr)

	def _updatePermission(id: Long, value: String, descr: Option[String]) =
		tablePermission
			.filter(_.id === id)
			.map(t => (t.value, t.descr))
			.update(value.trim.toLowerCase, descr)
			.map(_ == 1)

	override	def findPermissionById(id: Long): Future[Option[Permission]] =
		db.run(_findPermissionById(id))

	override def createPermission(value: String, descr: Option[String]): Future[Permission] =
		db.run(_createPermission(value, descr).transactionally)

	override def updatePermission(id: Long, value: String, descr: Option[String]): Future[Boolean] =
		db.run(_updatePermission(id, value, descr).transactionally)

	override def permissionExistsByValue(value: String): Future[Boolean] =
		db.run(_permissionExistsByValue(value))

	override def permissionsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
		db.run(_permissionsListPagesCount(pSize, filterOpt).result).map(pages(_, pSize))

	override def permissionsListPage(pageSize: Int, pageId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Permission]] =
		db.run(_permissionsListPage(pageSize, pageId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

	override def permissionsListPagesCountByTarget(targetId: Long, targetType: PermissionTargetTypes, pSize: Int, filterOpt: Option[String]): Future[Int] =
		db.run(_permissionsListPagesCountByTarget(targetId, targetType, pSize, filterOpt).result).map(pages(_, pSize))

	override def permissionsListPageByTarget(targetId: Long, targetType: PermissionTargetTypes, pageSize: Int, pageId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Permission]] =
		db.run(_permissionsListPageByTarget(targetId, targetType, pageSize, pageId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

	override def findPermissionsByTargetId(targetId: Long, targetType: PermissionTargetTypes): Future[Seq[Permission]] =
		db.run(_findPermissionsByTargetId(targetId, targetType).result)

	override def assignPermissionToTargetIfNotAssigned(permissionId: Long, targetId: Long, targetType: PermissionTargetTypes): Future[Boolean] =
		db.run(_assignPermissionToTargetIfNotAssigned(permissionId, targetId, targetType).transactionally)

	override def close: Future[Unit] =
		future(db.close)

}
