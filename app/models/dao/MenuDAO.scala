package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import play.api.inject.ApplicationLifecycle

trait MenuDAO {

  def updateMenu(id: Long, parentId: Option[Long], link: Option[String], name: String, content: Option[String], order: Int): Future[Boolean]

  def menusListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[models.Menu]]

  def menusListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

  def getMenuById(id: Long): Future[Option[models.Menu]]

  def close: Future[Unit]

}

class MenuDAOCloseHook @Inject() (dao: MenuDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}