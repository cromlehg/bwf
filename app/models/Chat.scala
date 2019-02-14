package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, _}
import play.api.mvc.WebSocket.MessageFlowTransformer
import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import play.api.libs.json.{Format, Json}
import play.engineio.EngineIOController
import play.api.libs.functional.syntax._
import play.socketio.scaladsl.SocketIO

import scala.collection.concurrent.TrieMap


object ChatProtocol {

	/**
		* A chat event, either a message, a join room, or a leave room event.
		*/
	sealed trait ChatEvent

	case class ChatMessageIn(msg: String) extends ChatEvent

	object ChatMessageIn {
		implicit val format: Format[ChatMessageIn] = Json.format
	}

	case class ChatMessageOut(login: String, msg: String, timstamp: Long) extends ChatEvent

	object ChatMessageOut {
		implicit val format: Format[ChatMessageOut] = Json.format
	}


	import play.socketio.scaladsl.SocketIOEventCodec._

	val decoder = decodeByName {
		case "chat message in" => decodeJson[ChatMessageIn]
	}

	val encoder = encodeByType[ChatMessageOut] {
		case _: ChatMessageOut => "chat message out" -> encodeJson[ChatMessageOut]
	}
}

case class NCMsgIn(msg: String)

case class NCMsgOut(val login: String, msg: String, val timestamp: String)

object NCMsg {

	implicit lazy val writesOut: Writes[NCMsgOut] = new Writes[NCMsgOut] {
		def writes(t: NCMsgOut) = Json.obj(
			"login" -> t.login,
			"msg" -> t.msg,
			"timestamp" -> t.timestamp
		)
	}

	implicit val readsIn: Reads[NCMsgIn] =
		(JsPath \ "msg").read[String].map(NCMsgIn)

	implicit val messageFlowTransformer =
		MessageFlowTransformer.jsonMessageFlowTransformer[NCMsgIn, NCMsgOut]

}

case class ChatMsg(val id: Long,
									 val ownerId: Long,
									 val msg: String,
									 val timestamp: Long,
									 val account: Option[Account])


object ChatMsg {


	def apply(id: Long,
						ownerId: Long,
						msg: String,
						timestamp: Long,
						account: Option[Account]) =
		new ChatMsg(id: Long,
			ownerId: Long,
			msg: String,
			timestamp: Long,
			account
		)

	def apply(id: Long,
						ownerId: Long,
						msg: String,
						timestamp: Long) =
		new ChatMsg(id: Long,
			ownerId,
			msg,
			timestamp,
			None)


}
