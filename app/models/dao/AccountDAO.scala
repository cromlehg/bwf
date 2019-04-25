package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import models.Account
import models.AccountStatus
import play.api.inject.ApplicationLifecycle

trait AccountDAO {

  def findAccountOptById(id: Long): Future[Option[Account]]

  def findAccountOptByLogin(login: String): Future[Option[Account]]

  def findAccountOptByLoginOrEmail(loginOrEmail: String): Future[Option[Account]]

  def findAccountOptWithRolesByLoginOrEmail(loginOrEmail: String): Future[Option[Account]]

  def findAccountOptBySessionKeyAndIPWithRoles(sessionKey: String, ip: String): Future[Option[Account]]

  def findAccountOptByConfirmCodeAndLogin(login: String, code: String): Future[Option[Account]]

  def createAccountWithRole(login: String, email: String, role: String): Future[Account]

  def isLoginExists(login: String): Future[Boolean]

  def isEmailExists(email: String): Future[Boolean]

	def findAccountsByIds(ids: Seq[Long]): Future[Seq[Account]]

  def emailVerified(login: String, code: String, approveData: String): Future[Option[Account]]

  def setAccountStatus(accountId: Long, status: AccountStatus.AccountStatus): Future[Boolean]

  def accountsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Account]]

  def accountsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

  def generatePasswordRecoveryCode(accountId: Long, code: String): Future[Boolean]

  def deletePasswordRecoveryCode(accountId: Long): Future[Boolean]

  def update(id: Long, password: String): Future[Boolean]

  def close: Future[Unit]

}

class AccountDAOCloseHook @Inject() (dao: AccountDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
