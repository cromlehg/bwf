package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.ChatMsg
import models.dao.ChatMsgDAO
import models.dao.slick.table.ChatMsgTable
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickChatMsgDAO @Inject()(
																 val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends ChatMsgDAO with ChatMsgTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	def _findById(id: Long) =
		queryById(id).result.headOption

	def _getLast(count: Int) =
		table.sortBy(_.timestamp.desc).take(count).result

	def _create(ownerId: Long, msg: String) =
		table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += ChatMsg(
			0,
			ownerId,
			msg,
			System.currentTimeMillis())

	def _update(id: Long, msg: String) =
		table
			.filter(_.id === id)
			.map(_.msg)
			.update(msg)
			.map(_ == 1)

	def _delete(id: Long) =
		table
			.filter(_.id === id)
			.delete
			.map(_ == 1)

	override def findById(id: Long): Future[Option[ChatMsg]] =
		db.run(_findById(id))

	override def create(ownerId: Long, msg: String): Future[ChatMsg] =
		db.run(_create(ownerId, msg).transactionally)

	override def update(id: Long, msg: String): Future[Boolean] =
		db.run(_update(id, msg).transactionally)

	override def delete(id: Long): Future[Boolean] =
		db.run(_delete(id).transactionally)

	def getLast(count: Int): Future[Seq[ChatMsg]] =
		db.run(_getLast(count: Int))


	override def close: Future[Unit] =
		future(db.close())

}
