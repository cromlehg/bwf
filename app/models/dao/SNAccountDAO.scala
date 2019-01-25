package models.dao

import javax.inject.Inject
import models.SNNAccountTypes.SNNAccountType
import models.SNAccount
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait SNAccountDAO {

	def close: Future[Unit]

	def snAccountExists(ownerId: Long, login: String, snType: SNNAccountType): Future[Boolean]

	def findSNAccountsById(id: Long): Future[Option[SNAccount]]

	def createSNAccount(ownerId: Long, login: String, snType: SNNAccountType): Future[SNAccount]

	def updateSNAccount(id: Long, login: String, snType: SNNAccountType): Future[Boolean]

	def removeById(id: Long): Future[Boolean]

}

class SNAccountDAOCloseHook @Inject()(dao: SNAccountDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
