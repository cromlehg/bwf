package models.dao.slick.table

trait ChatMsgTable extends CommonTable {

	import dbConfig.profile.api._

	class InnerCommonTable(tag: Tag) extends Table[models.ChatMsg](tag, "chat_msgs") with DynamicSortBySupport.ColumnSelector {
		def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

		def ownerId = column[Long]("owner_id")

		def msg = column[String]("msg")

		def timestamp = column[Long]("registered")

		def * = (
			id,
			ownerId,
			msg,
			timestamp) <> [models.ChatMsg](t => models.ChatMsg(
			t._1,
			t._2,
			t._3,
			t._4), t => Some(
			(t.id,
				t.ownerId,
				t.msg,
				t.timestamp)))

		override val select = Map(
			"id" -> (this.id),
			"ownerId" -> (this.ownerId),
			"msg" -> (this.msg),
			"timestamp" -> (this.timestamp))
	}

	val table = TableQuery[InnerCommonTable]

}
