package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject
import javax.inject.Singleton
import models.dao.OptionDAO
import models.dao.slick.table.OptionTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.Direction
import slick.ast.Ordering.Asc
import slick.ast.Ordering.Desc

@Singleton
class SlickOptionDAO @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends OptionDAO with OptionTable with SlickCommonDAO {

  import dbConfig.profile.api._
  import scala.concurrent.Future.{ successful => future }

  private val queryByName = Compiled(
    (name: Rep[String]) => table.filter(_.name === name))

  def _optionsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) = {
    table
      .filterOpt(filterOpt) { case (t, filter) => t.name.like("%" + filter.trim + "%") }
      .dynamicSortBy(sortsBy)
      .page(pSize, pId)
  }

  def _optionsListPagesCount(pSize: Int, filterOpt: Option[String]) = {
    table
      .filterOpt(filterOpt) { case (t, filter) => t.name.like("%" + filter.trim + "%") }
      .size
  }

  override def getOptions(): Future[Seq[models.BOption]] =
    db.run(table.result)

  override def getOptionByName(name: String): Future[Option[models.BOption]] =
    db.run(queryByName(name).result.headOption)

  override def updateOptionByName(name: String, value: String): Future[Option[models.BOption]] =
    db.run(table.filter(_.name === name).map(_.value).update(value).map(_ > 1)
      .flatMap(_ => queryByName(name).result.headOption))

  override def optionsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[models.BOption]] =
    db.run(_optionsListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

  override def optionsListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
    db.run(_optionsListPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))

  override def close: Future[Unit] =
    future(db.close())

}
