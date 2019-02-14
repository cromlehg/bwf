package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, _}
import play.api.mvc.WebSocket.MessageFlowTransformer

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
