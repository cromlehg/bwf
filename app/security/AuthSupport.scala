package security

import javax.inject.{Inject, Singleton}
import models.Account
import models.dao.AccountDAO

import scala.concurrent.Future

@Singleton
class AuthSupport @Inject()(accountDAO: AccountDAO) {

  def getAccount(sessionId: String, ip: String): Future[Option[Account]] =
    accountDAO.findAccountOptBySessionKeyAndIPWithRoles(sessionId, ip)

}