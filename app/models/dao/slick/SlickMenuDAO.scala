package models.dao.slick

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Random

import org.mindrot.jbcrypt.BCrypt

import controllers.AppConstants
import javax.inject.Inject
import javax.inject.Singleton
import models.Menu
import models.dao.MenuDAO
import models.dao.slick.table.MenuTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.Direction
import slick.ast.Ordering.Asc
import slick.ast.Ordering.Desc
import slick.sql.SqlAction

@Singleton
class SlickMenuDAO @Inject() (
  val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends MenuDAO with MenuTable with SlickCommontDAO {

  import dbConfig.profile.api._
  import scala.concurrent.Future.{ successful => future }

  private val queryById = Compiled(
    (id: Rep[Long]) => table.filter(_.id === id))

  def _findMenuById(id: Long) =
    queryById(id).result.headOption

  def _removeMenuById(id: Long) =
    queryById(id).delete.map(_ == 1)

  def _getMenuById(id: Long) =
    table.filter(_.menuId === id)

  def _updateMenu(id: Long, parentId: Option[Long], link: Option[String], name: String, content: Option[String], order: Int) =
    table
      .filter(_.id === id)
      .map(t => (t.parentId, t.link, t.name, t.content, t.order))
      .update(parentId, link, name, content, order)
      .map(_ == 1)

  def _menusListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String]) = {
    table
      .filter(_.parentId.isEmpty)
      .filterOpt(filterOpt) {
        case (t, filter) =>
          t.name.like("%" + filter.trim + "%") ||
            t.content.like("%" + filter.trim + "%") ||
            t.link.like("%" + filter.trim + "%")
      }
      .dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
      .page(pSize, pId)
  }

  def _menusListPagesCount(pSize: Int, filterOpt: Option[String]) = {
    table
      .filter(_.parentId.isEmpty)
      .filterOpt(filterOpt) {
        case (t, filter) =>
          t.name.like("%" + filter.trim + "%") ||
            t.content.like("%" + filter.trim + "%") ||
            t.link.like("%" + filter.trim + "%")
      }
      .size
  }

  override def getMenuById(id: Long): Future[Option[models.Menu]] = 
    db.run(_getMenuById(id).result).map { menuItems =>         
      menuItems.find(_.parentId.isEmpty).map(_.assembly(menuItems))
    }

  override def updateMenu(id: Long, parentId: Option[Long], link: Option[String], name: String, content: Option[String], order: Int): Future[Boolean] =
    db.run(_updateMenu(id, parentId, link, name, content, order).transactionally)

  override def menusListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String]): Future[Seq[models.Menu]] =
    db.run(_menusListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

  override def menusListPagesCount(pSize: Int, filterOpt: Option[String]): Future[Int] =
    db.run(_menusListPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))

  override def close: Future[Unit] =
    future(db.close())

}
