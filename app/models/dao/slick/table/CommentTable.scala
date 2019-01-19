package models.dao.slick.table

import models.{Comment, CommentContentTypes, CommentStatusTypes, CommentTargetTypes}

trait CommentTable extends CommonTable {

	import dbConfig.profile.api._

	implicit val CommentContentTimeMapper = enum2String(CommentContentTypes)

	implicit val CommentStatusTypeMapper = enum2String(CommentStatusTypes)

	implicit val CommentTargetTypeMapper = enum2String(CommentTargetTypes)

	class InnerCommonTable(tag: Tag) extends Table[Comment](tag, "comments") with DynamicSortBySupport.ColumnSelector {
		def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

		def ownerId = column[Long]("owner_id")

		def targetType = column[CommentTargetTypes.CommentTargetTypes]("target_type")

		def targetId = column[Long]("target_id")

		def parentId = column[Option[Long]]("parent_id")

		def contentType = column[CommentContentTypes.CommentTargetTypes]("content_type")

		def content = column[String]("content")

		def status = column[CommentStatusTypes.CommentStatus]("status")

		def created = column[Long]("created")

		def * = (
			id,
			ownerId,
			targetType,
			targetId,
			parentId,
			contentType,
			content,
			status,
			created) <>[models.Comment](t => models.Comment(
			t._1,
			t._2,
			t._3,
			t._4,
			t._5,
			t._6,
			t._7,
			t._8,
			t._9), t => Some(
			(t.id,
				t.ownerId,
				t.targetType,
				t.targetId,
				t.parentId,
				t.contentType,
				t.content,
				t.status,
				t.created)))

		override val select = Map(
			"content" -> (this.content))
	}

	val table = TableQuery[InnerCommonTable]

}
