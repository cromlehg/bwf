package models.dao.slick.table

trait PlatformProjectTable extends CommonTable {

	import dbConfig.profile.api._

	class InnerCommonTable(tag: Tag) extends Table[models.PlatformProject](tag, "platform_projects") with DynamicSortBySupport.ColumnSelector {
		def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

		def name = column[String]("name")

		def userId = column[Long]("login")

		def userLogin = column[String]("login")

		def gitURL = column[String]("git_url")

		def gitLogin = column[String]("git_login")

		def gitPwd = column[String]("git_pwd")

		def dbName = column[String]("db_name")

		def dbUser = column[String]("db_user")

		def dbPwd = column[String]("db_pass")

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
			t._12), t => Some((
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
