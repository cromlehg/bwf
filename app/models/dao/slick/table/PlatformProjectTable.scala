package models.dao.slick.table

import models.PlatformProjectStatuses

trait PlatformProjectTable extends CommonTable {

	import dbConfig.profile.api._

	implicit val PlatformProjectStatusMapper = enum2String(PlatformProjectStatuses)

	class InnerCommonTable(tag: Tag) extends Table[models.PlatformProject](tag, "platform_projects") with DynamicSortBySupport.ColumnSelector {
		def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

		def name = column[String]("name")

		def userId = column[Long]("user_id")

		def userLogin = column[String]("user_login")

		def gitURL = column[String]("git_url")

		def gitLogin = column[Option[String]]("git_login")

		def gitPwd = column[Option[String]]("git_pwd")

		def dbName = column[Option[String]]("db_name")

		def dbUser = column[Option[String]]("db_user")

		def dbPwd = column[Option[String]]("db_pass")

		def port = column[Long]("port")

		def status = column[PlatformProjectStatuses.PlatformProjectStatus]("status")

		def descr = column[Option[String]]("descr")

		def registered = column[Long]("registered")

		def * = (
			id,
			name,
			userId,
			userLogin,
			gitURL,
			gitLogin,
			gitPwd,
			dbName,
			dbUser,
			dbPwd,
			port,
			status,
			descr,
			registered) <>[models.PlatformProject](t => models.PlatformProject(
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
			t._11,
			t._12,
			t._13,
			t._14), t => Some((
			t.id,
			t.name,
			t.userId,
			t.userLogin,
			t.gitURL,
			t.gitLogin,
			t.gitPwd,
			t.dbName,
			t.dbUser,
			t.dbPass,
			t.port,
			t.status,
			t.descr,
			t.registered)))

		override val select = Map(
			"id" -> (this.id),
			"name" -> (this.name),
			"user_id" -> (this.userId),
			"user_login" -> (this.userLogin))

	}

	val table = TableQuery[InnerCommonTable]

}
