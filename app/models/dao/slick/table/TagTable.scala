package models.dao.slick.table

import models.TagTargetTypes

trait TagTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTableTag(tag: Tag) extends Table[models.Tag](tag, "tags")  with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def descr = column[Option[String]]("descr")

    def * = (
      id,
      name,
      descr) <>[models.Tag](t => models.Tag(
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

  implicit val TagTargetTypesMapper = enum2String(TagTargetTypes)

  class InnerCommonTableTagToTarget(tag: Tag) extends Table[(Long, TagTargetTypes.TagTargetTypes, Long)](tag, "tags_to_targets")  with DynamicSortBySupport.ColumnSelector {
    def tagId = column[Long]("tag_id")

    def targetType = column[TagTargetTypes.TagTargetTypes]("target_type")

    def targetId = column[Long]("target_id")

    def * = (tagId, targetType, targetId)

   override val select = Map(
    "tagId" -> (this.tagId),
    "targetType" -> (this.targetType),
    "targetId" -> (this.targetId))
  }

  val tableTag = TableQuery[InnerCommonTableTag]

  val tableTagToTarget = TableQuery[InnerCommonTableTagToTarget]

}
