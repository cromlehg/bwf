package models.dao.slick.table

import models.{Permission, PermissionTargetTypes}

trait PermissionTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTablePermission(tag: Tag) extends Table[Permission](tag, "permissions")  with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def value = column[String]("value")
    def descr = column[Option[String]]("descr")
    def * = (id, value, descr) <> [Permission](t => Permission.apply(t._1, t._2, t._3), Permission.unapply)

  override val select = Map(
    "id" -> (this.id),
    "value" -> (this.value),
    "descr" -> (this.descr))

  }

  implicit val PermissionTargetTypesMapper = enum2String(PermissionTargetTypes)

  class InnerCommonTablePermissionToTarget(tag: Tag) extends Table[(Long, PermissionTargetTypes.PermissionTargetTypes, Long)](tag, "permissions_to_targets")  with DynamicSortBySupport.ColumnSelector {
    def permissionId = column[Long]("permission_id")
    def targetType = column[PermissionTargetTypes.PermissionTargetTypes]("target_type")
    def targetId = column[Long]("target_id")
    def * = (permissionId, targetType, targetId)

   override val select = Map(
    "permissionId" -> (this.permissionId),
    "targetType" -> (this.targetType),
    "targetId" -> (this.targetId))

  }

  val tablePermission = TableQuery[InnerCommonTablePermission]

  val tablePermissionToTarget = TableQuery[InnerCommonTablePermissionToTarget]



}
