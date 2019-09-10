package models.dao

import javax.inject.Inject
import models.PlatformUser
import models.PlatformError
import models.PlatformUserSystemStatuses.PlatformUserSystemStatus
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait PlatformUserDAO {

	def findPlatformUserOptById(id: Long): Future[Option[PlatformUser]]

	def findPlatformUserOptByLogin(login: String): Future[Option[PlatformUser]]

	def createPlatformUser(login: String,
												 creatorId: Long): Future[Either[PlatformError, PlatformUser]]

	def updatePlatformUser(login: String,
												 systemStatus: PlatformUserSystemStatus): Future[Option[PlatformUser]]

	def platformUsersListPage(pSize: Int,
														pId: Int,
														sortsBy: Seq[(String, Boolean)],
														filterOpt: Option[String]): Future[Seq[models.PlatformUser]]

	def platformUsersListPagesCount(pSize: Int,
																	filterOpt: Option[String]): Future[Int]

	def close: Future[Unit]

}

class PlatformUserDAOCloseHook @Inject()(dao: PlatformUserDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
