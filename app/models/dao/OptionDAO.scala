package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import play.api.inject.ApplicationLifecycle

trait OptionDAO {
  def getOptions(): Future[Seq[models.BOption]]

  def getOptionByName(name: String): Future[Option[models.BOption]]

  def updateOptionByName(name: String, value: String): Future[Option[models.BOption]]

  def optionsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[models.BOption]]

  def optionsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int]

  def close: Future[Unit]

}

class OptionDAOCloseHook @Inject() (dao: OptionDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
