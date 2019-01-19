package models.dao.slick.table

import models.AccountStatus
import models.ConfirmationStatus

trait AccountTable extends CommonTable {

  import dbConfig.profile.api._
  
  implicit val AccountStatusMapper = enum2String(AccountStatus)
  
  implicit val ConfirmationStatusMapper = enum2String(ConfirmationStatus)

  class InnerCommonTable(tag: Tag) extends Table[models.Account](tag, "accounts")  with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def login = column[String]("login")
    def email = column[String]("email")
    def hash = column[Option[String]]("hash")
    def confirmationStatus = column[ConfirmationStatus.ConfirmationStatus]("confirmation_status")
    def accountStatus = column[AccountStatus.AccountStatus]("account_status")
    def registered = column[Long]("registered")
    def confirmCode = column[Option[String]]("confirm_code")
    def passwordRecoveryCode = column[Option[String]]("password_recovery_code")
    def passwordRecoveryDate = column[Option[Long]]("password_recovery_date")

    def * = (
      id,
      login,
      email,
      hash,
      confirmationStatus,
      accountStatus,
      registered,
      confirmCode,
      passwordRecoveryCode,
      passwordRecoveryDate) <> [models.Account](t => models.Account(
            t._1,
            t._2,
            t._3,
            t._4,
            t._5,
            t._6,
            t._7,
            t._8,
            t._9,
            t._10), t => Some((
      t.id,
      t.login,
      t.email,
      t.hash,
      t.confirmationStatus,
      t.accountStatus,
      t.registered,
      t.confirmCode,
      t.passwordRecoveryCode,
      t.passwordRecoveryDate)))

    override val select = Map(
      "id" -> (this.id),
      "login" -> (this.login),
      "email" -> (this.email),
      "confirmation_status" -> (this.confirmationStatus),
      "account_status" -> (this.accountStatus),
      "registered" -> (this.registered))
  }

  val table = TableQuery[InnerCommonTable]
  
}
