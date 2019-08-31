package models.dao.slick.table

import models.PlatformUserSystemStatuses

trait PlatformUserTable extends CommonTable {

  import dbConfig.profile.api._

  implicit val PlatformUserSystemStatusMapper = enum2String(PlatformUserSystemStatuses)

  class InnerCommonTable(tag: Tag) extends Table[models.PlatformUser](tag, "platform_users")  with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def login = column[String]("login")
		def systemStatus = column[PlatformUserSystemStatuses.PlatformUserSystemStatus]("system_status")
		def creatorId = column[Long]("creator_id")
    def registered = column[Long]("registered")

    def * = (
      id,
      login,
			systemStatus,
      creatorId,
      registered) <> [models.PlatformUser](t => models.PlatformUser(
            t._1,
            t._2,
            t._3,
            t._4,
            t._5), t => Some((
      t.id,
      t.login,
			t.systemStatus,
      t.creatorId,
      t.registered)))

    override val select = Map(
      "id" -> (this.id),
      "login" -> (this.login),
      "creator_id" -> (this.creatorId),
      "system_status" -> (this.systemStatus),
      "registered" -> (this.registered))

  }

  val table = TableQuery[InnerCommonTable]

}
