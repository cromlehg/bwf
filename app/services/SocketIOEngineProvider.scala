package services

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl._
import controllers.LoggerSupport
import javax.inject.{Inject, Provider, Singleton}
import models.dao.ChatMsgDAO
import models.{Account, Session}
import play.engineio.EngineIOController
import play.socketio.scaladsl.SocketIO
import security.AuthSupport

import scala.concurrent.ExecutionContext


@Singleton
class MySocketIOEngineProvider @Inject()(authSupport: AuthSupport,
																				 socketIO: SocketIO,
																				 chatMsgDAO: ChatMsgDAO,
																				 inputSanitizer: InputSanitizer)(implicit mat: Materializer,
																																				 ec: ExecutionContext)
	extends Provider[EngineIOController]
		with LoggerSupport {

	import models.ChatProtocol._

	import scala.concurrent.Future.{successful => future}

	val chatFlow: Flow[ChatMessageOut, ChatMessageOut, NotUsed] = {
		val (sink, source) = MergeHub.source[ChatMessageOut].toMat(BroadcastHub.sink)(Keep.both).run()
		Flow.fromSinkAndSourceCoupled(sink, source)
	}

	private def notAuthUserChatFlow: Flow[ChatMessageIn, ChatMessageOut, NotUsed] =
		Flow[ChatMessageIn]
			.filter(_ => false)
			.map(_.asInstanceOf[ChatMessageOut])
			.via(chatFlow).recoverWithRetries(-1, { case _: Exception ⇒ Source.empty })

	private def authUserChatFlow(account: Account): Flow[ChatMessageIn, ChatMessageOut, NotUsed] =
		Flow[ChatMessageIn].mapAsync(1) { t =>
			chatMsgDAO.create(account.id, inputSanitizer.sanitize(t.msg)).map { chatMsg =>
				ChatMessageOut(account.login,
					chatMsg.msg,
					chatMsg.timestamp)
			}
		}.via(chatFlow).recoverWithRetries(-1, { case _: Exception ⇒ Source.empty })

	override lazy val get = socketIO.builder
		.onConnectAsync { (request, sessionId) =>
			request.session.get(Session.TOKEN) match {
				case Some(token) =>
					val sessionKey = new String(java.util.Base64.getDecoder.decode(token))
					authSupport.getAccount(sessionKey, request.remoteAddress)
				case _ => future(None)
			}
		}.defaultNamespace(decoder, encoder) { session =>
		session.data.fold(notAuthUserChatFlow)(authUserChatFlow)
	}.createController()

}



