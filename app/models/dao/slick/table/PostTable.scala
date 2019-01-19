package models.dao.slick.table

import models.PostStatus

trait PostTable extends CommonTable {

  import dbConfig.profile.api._
  
  implicit val StatusNameMapper = enum2String(PostStatus)

  class InnerCommonTable(tag: Tag) extends Table[models.Post](tag, "posts") with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Long]("owner_id")
    def title = column[String]("title")
    def thumbnail = column[Option[String]]("thumbnail")
    def content = column[String]("content")
    def created = column[Long]("created")
    def status = column[PostStatus.PostStatus]("status")
    def * = (
        id, 
        ownerId,
        title,
        thumbnail,
        content,
        status,
        created) <> [models.Post](t => models.Post(
            t._1, 
            t._2,
            t._3,
            t._4,
            t._5,
            t._6,
            t._7), t => Some(
            (t.id, 
            t.ownerId,
            t.title,
            t.thumbnail,
            t.content,
            t.status,
            t.created)))
    override val select = Map(
      "id" -> (this.id),
      "title" -> (this.title),
      "content" -> (this.content))
  }

  val table = TableQuery[InnerCommonTable]
  
}
