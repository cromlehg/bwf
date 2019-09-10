package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.PlatformUserSystemStatuses.PlatformUserSystemStatus
import models._
import models.dao.PlatformUserDAO
import models.dao.slick.table.PlatformUserTable
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}
import slick.sql.SqlAction

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickPlatformUserDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends PlatformUserDAO with PlatformUserTable with SlickCommonDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	private val queryByLogin = Compiled(
		(login: Rep[String]) => table.filter(_.login === login))

	def _findPlatformUserOptById(id: Long): SqlAction[Option[PlatformUser], NoStream, Effect.Read] =
		queryById(id).result.headOption

	def _findPlatformUserOptByLogin(login: String): SqlAction[Option[PlatformUser], NoStream, Effect.Read] =
		queryByLogin(login).result.headOption

	def _createPlatformUserWithoutCheck(login: String,
																			creatorId: Long) =
		table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += models.PlatformUser(
			0,
			login,
			PlatformUserSystemStatuses.RESOLVED,
			creatorId,
			System.currentTimeMillis)

	def _createPlatformUser(login: String,
													creatorId: Long) =
		_findPlatformUserOptByLogin(login) flatMap (_ match {
			case Some(_) =>
				DBIO.successful(Left(PlatformError("Platform user with login " + login + " already exists")))
			case _ =>
				_createPlatformUserWithoutCheck(login, creatorId).map { user =>
					Right(user)
				}
		})

	//		for {
	//			SlickEitherT(_findPlatformUserOptByLogin(login).map( t => t._.toRight("Platform user with login " + login + " already exists")))
	//			mc <- SlickEitherT(_createPlatformUserWithoutCheck(login, creatorId).map(Right(_)))
	//		} yield mc

	def _updatePlatformUser(login: String,
													systemStatus: PlatformUserSystemStatuses.PlatformUserSystemStatus) =
		for {
			_ <- table
				.filter(_.login === login)
				.map(_.systemStatus)
				.update(systemStatus)
			pu <- _findPlatformUserOptByLogin(login)
		} yield pu

	def _platformUsersListPage(pSize: Int,
														 pId: Int,
														 sortsBy: Seq[(String, Direction)],
														 filterOpt: Option[String]) =
		table
			.filterOpt(filterOpt) {
				case (t, filter) => t.login.like("%" + filter.trim + "%")
			}
			.dynamicSortBy(sortsBy)
			.page(pSize, pId)

	def _platformUsersListPagesCount(pSize: Int,
																	 filterOpt: Option[String]) =
		table
			.filterOpt(filterOpt) {
				case (t, filter) => t.login.like("%" + filter.trim + "%")
			}
			.size

	override def findPlatformUserOptById(id: Long): Future[Option[PlatformUser]] =
		db.run(_findPlatformUserOptById(id))

	override def findPlatformUserOptByLogin(login: String): Future[Option[PlatformUser]] =
		db.run(_findPlatformUserOptByLogin(login))

	override def createPlatformUser(login: String,
																	creatorId: Long): Future[Either[PlatformError, PlatformUser]] =
		db.run(_createPlatformUser(login, creatorId).transactionally)

	override def updatePlatformUser(login: String,
																	systemStatus: PlatformUserSystemStatus): Future[Option[PlatformUser]] =
		db.run(_updatePlatformUser(login, systemStatus).transactionally)

	override def platformUsersListPage(pSize: Int,
																		 pId: Int,
																		 sortsBy: Seq[(String, Boolean)],
																		 filterOpt: Option[String]): Future[Seq[models.PlatformUser]] =
		db.run(_platformUsersListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt).result)

	override def platformUsersListPagesCount(pSize: Int,
																					 filterOpt: Option[String]): Future[Int] =
		db.run(_platformUsersListPagesCount(pSize, filterOpt).result).map(t => pages(t, pSize))

	override def close: Future[Unit] =
		future(db.close())

}
