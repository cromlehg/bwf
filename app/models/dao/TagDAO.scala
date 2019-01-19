package models.dao

import javax.inject.Inject
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait TagDAO {

  def findTagById(id: Long): Future[Option[models.Tag]]

  def createTag(name: String, descr: Option[String]): Future[models.Tag]

  def search(query: String): Future[Seq[models.Tag]]

  def close: Future[Unit]

}

class TagDAOCloseHook @Inject()(dao: TagDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
