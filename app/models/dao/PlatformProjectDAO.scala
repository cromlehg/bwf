package models.dao

import javax.inject.Inject
import models.{PlatformError, PlatformProject, PlatformProjectStatuses}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait PlatformProjectDAO {

	def findPlatformProjectOptById(id: Long): Future[Option[PlatformProject]]

	def findPlatformProjectOptByName(login: String): Future[Option[PlatformProject]]

	def findPlatformProjectsByUserId(userId: Long): Future[Seq[PlatformProject]]

	def createPlatformProject(name: String,
														userId: Long,
														userLogin: String,
														gitURL: String,
														gtiLogin: Option[String],
														gitPwd: Option[String],
														port: Long,
														descr: Option[String]): Future[Either[PlatformError, PlatformProject]]

	def updatePlatformDBProps(id: Long,
														dbName: Option[String],
														dbUser: Option[String],
														dbPass: Option[String]): Future[Either[PlatformError, PlatformProject]]

	def updatePlatformProjectStatus(id: Long,
																	status: PlatformProjectStatuses.PlatformProjectStatus): Future[Either[PlatformError, PlatformProject]]

	def platformProjectsListPage(pSize: Int,
															 pId: Int,
															 sortsBy: Seq[(String, Boolean)],
															 filterOpt: Option[String]): Future[Seq[models.PlatformProject]]

	def platformProjectsListPagesCount(pSize: Int,
																		 filterOpt: Option[String]): Future[Int]

	def close: Future[Unit]

}

class PlatformProjectDAOCloseHook @Inject()(dao: PlatformProjectDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
