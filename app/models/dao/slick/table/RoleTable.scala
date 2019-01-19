package models.dao.slick.table

import models.{Role, RoleTargetTypes}

trait RoleTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTableRole(tag: Tag) extends Table[Role](tag, "roles")  with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def descr = column[Option[String]]("descr")

    def * = (
      id,
      name,
      descr) <>[Role](t => Role(
      t._1,
      t._2,
      t._3), t => Some(
      (t.id,
        t.name,
        t.descr)))

  override val select = Map(
    "id" -> (this.id),
    "name" -> (this.name),
    "descr" -> (this.descr))

  }

  implicit val RoleTargetTypesMapper = enum2String(RoleTargetTypes)

  class InnerCommonTableRoleToTarget(tag: Tag) extends Table[(Long, RoleTargetTypes.RoleTargetTypes, Long)](tag, "roles_to_targets")  with DynamicSortBySupport.ColumnSelector {
    def roleId = column[Long]("role_id")

    def targetType = column[RoleTargetTypes.RoleTargetTypes]("target_type")

    def targetId = column[Long]("target_id")

    def * = (roleId, targetType, targetId)

   override val select = Map(
    "roleId" -> (this.roleId),
    "targetType" -> (this.targetType),
    "targetId" -> (this.targetId))
  }

  val tableRole = TableQuery[InnerCommonTableRole]

  val tableRoleToTarget = TableQuery[InnerCommonTableRoleToTarget]

}
