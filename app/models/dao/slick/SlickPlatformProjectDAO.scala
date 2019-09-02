package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.PlatformProject
import models.dao.PlatformProjectDAO
import models.dao.slick.table.PlatformProjectTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}
import slick.sql.SqlAction

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickPlatformProjectDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends PlatformProjectDAO with PlatformProjectTable with SlickCommonDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	private val queryByName = Compiled(
		(name: Rep[String]) => table.filter(_.name === name))

	private val queryByUserId = Compiled(
		(userId: Rep[Long]) => table.filter(_.userId === userId))

	def _findPlatformProjectOptById(id: Long): SqlAction[Option[PlatformProject], NoStream, Effect.Read] =
		queryById(id).result.headOption

	def _findPlatformProjectOptByName(name: String): SqlAction[Option[PlatformProject], NoStream, Effect.Read] =
		queryByName(name).result.headOption

	def _findPlatformProjectsByUserId(userId: Long): SqlAction[Seq[PlatformProject], NoStream, Effect.Read] =
		queryByUserId(userId).result


	def _createPlatformProjectWithoutCheck(name: String,
																				 userId: Long,
																				 userLogin: String,
																				 gitURL: String,
																				 gtiLogin: String,
																				 gitPwd: String,
																				 dbName: String,
																				 dbUser: String,
																				 dbPass: String,
																				 descr: Option[String]) =
		table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += models.PlatformProject(
			0,
			name,
			userId,
			userLogin,
			gitURL,
			gtiLogin,
			gitPwd,
			dbName,
			dbUser,
			dbPass,
			descr,
			System.currentTimeMillis)

	def _createPlatformProject(name: String,
														 userId: Long,
														 userLogin: String,
														 gitURL: String,
														 gtiLogin: String,
														 gitPwd: String,
														 dbName: String,
														 dbUser: String,
														 dbPass: String,
														 descr: Option[String]) =
		_findPlatformProjectOptByName(name) flatMap (_ match {
			case Some(_) =>
				DBIO.successful(Left("Platform project with name " + name + " already exists"))
			case _ =>
				_createPlatformProjectWithoutCheck(name,
					userId,
					userLogin,
					gitURL,
					gtiLogin,
					gitPwd,
					dbName,
					dbUser,
					dbPass,
					descr).map(Right.apply)
		})

	def _platformProjectsListPage(pSize: Int,
																pId: Int,
																sortsBy: Seq[(String, Direction)],
																filterOpt: Option[String]) =
		table
			.filterOpt(filterOpt) {
				case (t, filter) => t.name.like("%" + filter.trim + "%")
			}
			.dynamicSortBy(sortsBy)
			.page(pSize, pId)

	def _platformProjectsListPagesCount(pSize: Int,
																			filterOpt: Option[String]) =
		table
			.filterOpt(filterOpt) {
				case (t, filter) => t.name.like("%" + filter.trim + "%")
			}
			.size

	override def findPlatformProjectOptById(id: Long): Future[Option[PlatformProject]] =
		db.run(_findPlatformProjectOptById(id))

	override def findPlatformProjectOptByName(name: String): Future[Option[PlatformProject]] =
		db.run(_findPlatformProjectOptByName(name))

	override def findPlatformProjectsByUserId(userId: Long): Future[Seq[PlatformProject]] =
		db.run(_findPlatformProjectsByUserId(userId))

	override def createPlatformProject(name: String,
																		 userId: Long,
																		 userLogin: String,
																		 gitURL: String,
																		 gtiLogin: String,
																		 gitPwd: String,
																		 dbName: String,
																		 dbUser: String,
																		 dbPass: String,
																		 descr: Option[String]): Future[Either[String, PlatformProject]] =
		db.run(_createPlatformProject(name,
			userId,
			userLogin,
			gitURL,
			gtiLogin,
			gitPwd,
			dbName,
			dbUser,
			dbPass,
			descr).transactionally)

	override def platformProjectsListPage(pSize: Int,
																				pId: Int,
																				sortsBy: Seq[(String, Boolean)],
																				filterOpt: Option[String]): Future[Seq[models.PlatformProject]] =
		db.run(_platformProjectsListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

	override def platformProjectsListPagesCount(pSize: Int,
																							filterOpt: Option[String]): Future[Int] =
		db.run(_platformProjectsListPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))

	override def close: Future[Unit] =
		future(db.close())

}
