package models.dao

import javax.inject.Inject
import models.ChatMsg
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait ChatMsgDAO {

	def update(id: Long, msg: String): Future[Boolean]

	def findById(id: Long): Future[Option[ChatMsg]]

	def getLast(count: Int): Future[Seq[ChatMsg]]

	def delete(id: Long): Future[Boolean]

	def create(ownerId: Long,
						 msg: String): Future[ChatMsg]

	def close: Future[Unit]

}

class ChatMsgDAOCloseHook @Inject()(dao: ChatMsgDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
