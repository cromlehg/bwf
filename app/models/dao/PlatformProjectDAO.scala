package models.dao

import javax.inject.Inject
import models.PlatformProject
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
														gtiLogin: String,
														gitPwd: String,
														dbName: String,
														dbUser: String,
														dbPass: String,
														descr: Option[String]): Future[Either[String, PlatformProject]]


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
