package models.dao.slick.table

import models.{SNAccount, SNNAccountTypes}

trait SNAccountTable extends CommonTable {

  import dbConfig.profile.api._

	implicit val snAccountsTypeMapper = enum2String(SNNAccountTypes)


	class InnerCommonTable(tag: Tag) extends Table[SNAccount](tag, "sn_accounts") with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Long]("owner_id")
    def snType = column[SNNAccountTypes.SNNAccountType]("sn_type")
    def login = column[String]("login")
    def registered = column[Long]("registered")

		def * = (
			id,
			ownerId,
			snType,
			login,
			registered) <> [SNAccount](t => SNAccount(
			t._1,
			t._2,
			t._3,
			t._4,
			t._5), t => Some(
			(t.id,
				t.ownerId,
				t.snType,
				t.login,
				t.registered)))

    override val select = Map(
      "id" -> (this.id),
      "ownerId" -> (this.ownerId),
      "snType" -> (this.snType),
      "login" -> (this.login),
      "registered" -> (this.registered))
  }

  val table = TableQuery[InnerCommonTable]
  
}
