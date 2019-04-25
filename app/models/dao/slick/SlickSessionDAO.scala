package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.dao.SessionDAO
import models.dao.slick.table.SessionTable
import play.api.db.slick.DatabaseConfigProvider
import slick.sql.SqlAction

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSessionDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends SessionDAO with SessionTable with SlickCommonDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryByKeyAndIp = Compiled(
		(key: Rep[String], ip: Rep[String]) => table.filter(t => t.sessionKey === key && t.ip === ip))

	private val queryByAccountIdAndKeyAndIp = Compiled(
		(id: Rep[Long], key: Rep[String], ip: Rep[String]) =>
			table.filter(t => t.sessionKey === key && t.ip === ip && t.userId === id))

	def _findSessionOptByKeyAndIp(sessionKey: String, ip: String): SqlAction[Option[models.Session], NoStream, Effect.Read] =
		queryByKeyAndIp((sessionKey, ip)).result.headOption

	def _invalidateSessionBySessionKeyAndIP(sessionKey: String, ip: String) =
		table
			.filter(t => t.sessionKey === sessionKey && t.ip === ip)
			.map(_.expire)
			.update(System.currentTimeMillis) map (r => if (r == 1) true else false)

	def _findSessionByAccountIdSessionKeyAndIP(id: Long,
																						 key: String,
																						 ip: String): SqlAction[Option[models.Session], NoStream, Effect.Read] =
		queryByAccountIdAndKeyAndIp((id, key, ip)).result.headOption

	def _create(accountId: Long,
							ip: String,
							userAgent: Option[String],
							os: Option[String],
							device: Option[String],
							sessionKey: String,
							created: Long,
							expire: Long) =
		(table returning table.map(_.id) into ((v, id) => v.copy(id = id))) += models.Session(
			0,
			accountId,
			ip,
			userAgent,
			os,
			device,
			sessionKey,
			created,
			expire)


	override def findSessionByAccountIdSessionKeyAndIP(id: Long,
																										 key: String,
																										 ip: String): Future[Option[models.Session]] =
		db.run(_findSessionByAccountIdSessionKeyAndIP(id, key, ip))

	override def invalidateSessionBySessionKeyAndIP(sessionKey: String, ip: String) =
		db.run(_invalidateSessionBySessionKeyAndIP(sessionKey, ip))

	override def create(accountId: Long,
											ip: String,
											userAgent: Option[String],
											os: Option[String],
											device: Option[String],
											sessionKey: String,
											created: Long,
											expire: Long): Future[Option[models.Session]] = {
		db.run(_create(
			accountId,
			ip,
			userAgent,
			os,
			device,
			sessionKey,
			created,
			expire)) map { dbSession => Some(dbSession) }
	}

	override def close: Future[Unit] =
		future(db.close())

}
