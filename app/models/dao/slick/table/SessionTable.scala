package models.dao.slick.table

trait SessionTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTable(tag: Tag) extends Table[models.Session](tag, "sessions") with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def ip = column[String]("ip")
		def userAgent = column[Option[String]]("user_agent")
		def os = column[Option[String]]("os")
		def device = column[Option[String]]("device")
		def sessionKey = column[String]("session_key")
    def created = column[Long]("created")
    def expire = column[Long]("expire")
    def * = (id, userId, ip, userAgent, os, device, sessionKey, created, expire) <> (t =>
      models.Session(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8, t._9), models.Session.unapply)
    override val select = Map(
      "id" -> (this.id),
      "userId" -> (this.userId),
      "ip" -> (this.ip),
			"userAgent" -> (this.userAgent),
			"os" -> (this.os),
			"device" -> (this.device),
      "created" -> (this.created),
      "expire" -> (this.expire))
  }

  val table = TableQuery[InnerCommonTable]

}
