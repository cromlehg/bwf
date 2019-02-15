package models

import play.api.libs.json.{Format, Json}


object ChatProtocol {

	/**
		* A chat event, either a message, a join room, or a leave room event.
		*/
	sealed trait ChatEvent

	case class ChatMessageIn(msg: String) extends ChatEvent

	object ChatMessageIn {
		implicit val format: Format[ChatMessageIn] = Json.format
	}

	case class ChatMessageOut(val login: String,
														val msg: String,
														val timestamp: Long) extends ChatEvent

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
