package models.dao.slick

import javax.inject.{Inject, Singleton}
import models._
import models.dao.AccountDAO
import models.dao.slick.table.AccountTable
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}
import slick.sql.SqlAction

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class SlickAccountDAO @Inject()( val dbConfigProvider: DatabaseConfigProvider,
                                 val roleDAO: SlickRoleDAO,
                                 val sessionDAO: SlickSessionDAO)(implicit ec: ExecutionContext)
  extends AccountDAO with AccountTable with SlickCommonDAO {

  import dbConfig.profile.api._

  import scala.concurrent.Future.{successful => future}

  private val queryById = Compiled(
    (id: Rep[Long]) => table.filter(_.id === id))

  private val queryByLogin = Compiled(
    (login: Rep[String]) => table.filter(_.login === login))

  private val queryByEmail = Compiled(
    (login: Rep[String]) => table.filter(_.login === login))

  private val queryByLoginOrEmail = Compiled(
    (loginOrEmail: Rep[String]) => table.filter(t => t.login === loginOrEmail || t.email === loginOrEmail))

  private val queryByLoginAndConfirmCode = Compiled(
    (login: Rep[String], code: Rep[String]) => table.filter(t => t.login === login && t.confirmCode === code))

	def _findAccounts(ids: Seq[Long]) =
		table.filter(_.id inSet ids).result

  def _accountsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) = {
    table
      .filterOpt(filterOpt) {
        case (t, filter) =>
          t.login.like("%" + filter.trim + "%") ||
            t.email.like("%" + filter.trim + "%")
      }
      .dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
      .page(pSize, pId)
  }

  def _accountsListPagesCount(pSize: Int, filterOpt: Option[String]) = {
    table
      .filterOpt(filterOpt) {
        case (t, filter) =>
          t.login.like("%" + filter.trim + "%") ||
            t.email.like("%" + filter.trim + "%")
      }
      .size
  }

  def _findAccountOptByConfirmCodeAndLogin(login: String, code: String) =
    queryByLoginAndConfirmCode((login, code)).result.headOption

  def _findAccountOptById(id: Long): SqlAction[Option[Account], NoStream, Effect.Read] =
    queryById(id).result.headOption

  def _findAccountOptByLogin(login: String): SqlAction[Option[Account], NoStream, Effect.Read] =
    queryByLogin(login).result.headOption

  def _findAccountOptByLoginOrEmail(loginOrEmail: String): SqlAction[Option[Account], NoStream, Effect.Read] =
    queryByLoginOrEmail(loginOrEmail).result.headOption

  def _updateAccountWithRoles(account: Account) =
    roleDAO._findRolesWithPermissionsByTarget(account.id, RoleTargetTypes.ACCOUNT) map { t => Some(account.copy(roles = t.toList)) }

  def _isLoginExists(login: String) =
    queryByLogin(login).result.headOption.map(_.isDefined)

  def _isEmailExists(email: String) =
    queryByEmail(email).result.headOption.map(_.isDefined)

  def _findAccountOptWithRolesByLoginOrEmail(loginOrEmail: String) =
    _findAccountOptByLoginOrEmail(loginOrEmail) flatMap _updateAccountOptWithRoles

  def _findAccountOptWithRolesById(id: Long) =
    _findAccountOptById(id) flatMap _updateAccountOptWithRoles

  def _findAccountBySessionKeyAndIPWithRoles(sessionKey: String, ip: String) =
    for {
      sessionOpt <- sessionDAO._findSessionOptByKeyAndIp(sessionKey, ip)
      accountOpt <- maybeOptAction(sessionOpt)(t => _findAccountOptWithRolesById(t.userId))
    } yield accountOpt.map(_.copy(sessionOpt = sessionOpt))

  def _createAccount(login: String, email: String) =
    for {
      dbAccount <- (table returning table.map(_.id) into ((v, id) => v.copy(id = id))) += models.Account(
        0,
        login,
        email,
        None,
        ConfirmationStatus.WAIT_CONFIRMATION,
        AccountStatus.NORMAL,
        System.currentTimeMillis,
        Some(BCrypt.hashpw(Random.nextString(5) + login + System.currentTimeMillis.toString, BCrypt.gensalt())
          .replaceAll("\\.", "s")
          .replaceAll("\\\\", "d")
          .replaceAll("\\$", "g").toList.map(_.toInt.toHexString).mkString.substring(0, 99)),
        None,
        None)
    } yield dbAccount

  def _createAccountWithRole(login: String, email: String, role: String) =
    _createAccount(login, email) flatMap { account =>
      roleDAO._assignAndReturnRoleNameToTargetIfNotAssigned(role, account.id, RoleTargetTypes.ACCOUNT) map { roleOpt =>
        account.copy(roles = List(roleOpt.get))
      }
    }

  def _updateAccountOptWithRoles(accountOpt: Option[Account]) =
    maybeOptAction(accountOpt)(_updateAccountWithRoles)

  def _generatePasswordRecoveryCode(id: Long, code: String) =
    table
      .filter(_.id === id)
      .map(t => (t.passwordRecoveryCode, t.passwordRecoveryDate))
      .update(Some(code), Some(System.currentTimeMillis))
      .map(_ == 1)

  def _deletePasswordRecoveryCode(id: Long) =
    table
      .filter(_.id === id)
      .map(t => (t.passwordRecoveryCode, t.passwordRecoveryDate))
      .update(None, None)
      .map(_ == 1)

  def _update(id: Long, password: String) =
    table
      .filter(_.id === id)
      .map(t => t.hash)
      .update(Some(BCrypt.hashpw(password, BCrypt.gensalt())))
      .map(_ == 1)

	override def createAccountWithRole(login: String, email: String, role: String): Future[Account] =
    db.run(_createAccountWithRole(login, email, role).transactionally)

  override def findAccountOptById(id: Long): Future[Option[Account]] =
    db.run(_findAccountOptById(id))

  override def findAccountOptBySessionKeyAndIPWithRoles(sessionKey: String, ip: String): Future[Option[Account]] =
    db.run(_findAccountBySessionKeyAndIPWithRoles(sessionKey, ip))

  override def findAccountOptByLogin(login: String): Future[Option[Account]] =
    db.run(_findAccountOptByLogin(login))

  override def findAccountOptByLoginOrEmail(loginOrEmail: String): Future[Option[Account]] =
    db.run(_findAccountOptByLoginOrEmail(loginOrEmail))

  override def findAccountOptWithRolesByLoginOrEmail(loginOrEmail: String): Future[Option[Account]] =
    db.run(_findAccountOptWithRolesByLoginOrEmail(loginOrEmail))

  override def findAccountOptByConfirmCodeAndLogin(login: String, code: String): Future[Option[Account]] =
    db.run(_findAccountOptByConfirmCodeAndLogin(login, code))

  override def isLoginExists(login: String): Future[Boolean] =
    db.run(_isLoginExists(login))

  override def isEmailExists(email: String): Future[Boolean] =
    db.run(_isEmailExists(email))

  override def emailVerified(login: String, code: String, password: String): Future[Option[Account]] = {
    val query = for {
      isUpdated <- table.filter(t => t.login === login && t.confirmCode === code)
        .map(t => (t.confirmCode, t.confirmationStatus, t.hash))
        .update(None, ConfirmationStatus.CONFIRMED, Some(BCrypt.hashpw(password, BCrypt.gensalt())))
        .map(_ == 1)
      accountOpt <- isOpt(isUpdated)(_findAccountOptByLogin(login))
    } yield (accountOpt)

    db.run(query)
  }

	override def findAccountsByIds(ids: Seq[Long]): Future[Seq[Account]] =
		db.run(_findAccounts(ids))

  override def setAccountStatus(accountId: Long, status: AccountStatus.AccountStatus): Future[Boolean] =
    db.run(table
      .filter(_.id === accountId)
      .map(_.accountStatus)
      .update(status).transactionally).map(_ == 1)

  override def accountsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[Account]] =
    db.run(_accountsListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result flatMap { accounts =>
      roleDAO._findRolesWithPermissionsByTargetsWithAssigns(accounts.map(_.id), RoleTargetTypes.ACCOUNT) map { rolesWithAssigns =>
        accounts map { account =>
          account.copy(roles = rolesWithAssigns.filter(_._1 == account.id).map(_._2).toList)
        }
      }
    })

	override def accountsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
    db.run(_accountsListPagesCount(pSize, filterOpt).result).map(pages(_, pSize))

  override def generatePasswordRecoveryCode(accountId: Long, code: String): Future[Boolean] =
    db.run(_generatePasswordRecoveryCode(accountId, code))

  override def deletePasswordRecoveryCode(accountId: Long): Future[Boolean] =
    db.run(_deletePasswordRecoveryCode(accountId))

  override def update(id: Long, password: String): Future[Boolean] =
    db.run(_update(id, password))

  override def close: Future[Unit] =
    future(db.close())

}
