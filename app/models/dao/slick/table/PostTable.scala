package models.dao.slick.table

import models.PostStatus
import models.PostTypes

trait PostTable extends CommonTable {

  import dbConfig.profile.api._

  implicit val StatusNameMapper = enum2String(PostStatus)

	implicit val PostTypeNameMapper = enum2String(PostTypes)

  class InnerCommonTable(tag: Tag) extends Table[models.Post](tag, "posts") with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Long]("owner_id")
    def title = column[String]("title")
    def thumbnail = column[Option[String]]("thumbnail")
    def content = column[String]("content")
    def status = column[PostStatus.PostStatus]("status")
		def registered = column[Long]("created")
		def metaTitle = column[Option[String]]("meta_title")
		def metaDescr = column[Option[String]]("meta_descr")
		def metaKeywords = column[Option[String]]("meta_keywords")
		def postType = column[PostTypes.PostType]("post_type")
    def * = (
        id,
        ownerId,
        title,
        thumbnail,
        content,
        status,
				registered,
				metaTitle,
				metaDescr,
				metaKeywords,
				postType) <> [models.Post](t => models.Post(
            t._1,
            t._2,
            t._3,
            t._4,
            t._5,
            t._6,
            t._7,
						t._8,
						t._9,
						t._10,
						t._11), t => Some(
            (t.id,
            t.ownerId,
            t.title,
            t.thumbnail,
            t.content,
            t.status,
            t.registered,
						t.metaTitle,
						t.metaDescr,
						t.metaKeywords,
						t.postType)))

    override val select = Map(
      "id" -> (this.id),
      "title" -> (this.title),
      "content" -> (this.content),
			"meta_title" -> (this.metaTitle),
			"meta_descr" -> (this.metaDescr),
			"meta_keywords" -> (this.metaKeywords))
  }

  val table = TableQuery[InnerCommonTable]

}
