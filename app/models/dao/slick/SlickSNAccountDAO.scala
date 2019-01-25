package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.SNAccount
import models.SNNAccountTypes.SNNAccountType
import models.dao.SNAccountDAO
import models.dao.slick.table.SNAccountTable
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSNAccountDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends SNAccountDAO with SNAccountTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	private val querySNAccountsByOwnerId = Compiled(
		(ownerId: Rep[Long]) => table.filter(_.ownerId === ownerId))

	private val querySNAccountsExists = Compiled(
		(ownerId: Rep[Long], snType: Rep[SNNAccountType], login: Rep[String]) => table
			.filter(_.ownerId === ownerId)
			.filter(_.snType === snType)
			.filter(_.login === login)
			.exists)

	def _createSNAccount(ownerId: Long, login: String, snType: SNNAccountType) =
		table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += SNAccount(
			0,
			ownerId,
			snType,
			login,
			System.currentTimeMillis)

	def _snAccountExists(ownerId: Long, login: String, snType: SNNAccountType) =
		querySNAccountsExists(ownerId, snType, login)

	def _findSNAccountsByOwnerId(ownerId: Long) =
		table.filter(_.ownerId === ownerId).result

	def _findSNAccountsById(id: Long) =
		queryById(id).result.headOption

	def _removeById(id: Long) =
		queryById(id).delete.map(_ == 1)


	def _updateSNAccount(id: Long, login: String, snType: SNNAccountType) =
		table
			.filter(_.id === id)
			.map(t => (t.login, t.snType))
			.update(login, snType)
			.map(_ == 1)

	override def updateSNAccount(id: Long, login: String, snType: SNNAccountType): Future[Boolean] =
		db.run(_updateSNAccount(id, login, snType).transactionally)

	override def removeById(id: Long): Future[Boolean] =
		db.run(_removeById(id))

	override def findSNAccountsById(id: Long): Future[Option[SNAccount]] =
		db.run(_findSNAccountsById(id))

	override def createSNAccount(ownerId: Long, login: String, snType: SNNAccountType): Future[SNAccount] =
		db.run(_createSNAccount(ownerId, login, snType).transactionally)

	override def snAccountExists(ownerId: Long, login: String, snType: SNNAccountType): Future[Boolean] =
		db.run(_snAccountExists(ownerId, login, snType).result)

	override def close: Future[Unit] =
		future(db.close())

}
