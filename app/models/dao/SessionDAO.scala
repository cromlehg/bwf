package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import models.Session
import play.api.inject.ApplicationLifecycle

trait SessionDAO {

  def invalidateSessionBySessionKeyAndIP(sessionKey: String, ip: String): Future[Boolean]

  def findSessionByAccountIdSessionKeyAndIP(id: Long, key: String, ip: String): Future[Option[Session]]

  def create(
    accountId: Long,
    ip: String,
		userAgent: Option[String],
		os: Option[String],
		device: Option[String],
    sessionKey: String,
    created: Long,
    expire: Long): Future[Option[models.Session]]

  def close: Future[Unit]

}

class SessionDAOCloseHook @Inject() (dao: SessionDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
