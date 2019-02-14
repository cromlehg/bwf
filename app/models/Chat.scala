package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, _}
import play.api.mvc.WebSocket.MessageFlowTransformer


object ChatProtocol {
 
  val KIND_SEND_MSG = "send_msg"
  
  val KIND_RECEIVE_MSG = "receive_msg"
  
  val KIND_REJECTED = "rejected"
  
  case class CPPacket(val kind: String, val event: CPEvent)
  
  trait CPEvent
  
  case class CPSendMsg(val msg: String) extends CPEvent
  
  case class CPReceiveMsg(val login: String, val content: String, timestamp: String) extends CPEvent
  
  case class CPRejected(val reason: String) extends CPEvent

  implicit val formatCPSendMsg: Format[CPSendMsg] = Json.format[CPSendMsg] 

  implicit val formatCPReceiveMsg: Format[CPReceiveMsg] = Json.format[CPReceiveMsg] 

  implicit val formatCPRejected: Format[CPRejected] = Json.format[CPRejected]

//	implicit val cpPacketWriter: Writes[CPEvent] = new Writes[CPEvent] {
//		def writes(t: CPEvent) =
//			if(t.isInstanceOf[CPSendMsg])
//				Json.obj(t.asInstanceOf[CPSendMsg])
//			} else {
//				Json.obj(t.asInstanceOf[CPSendMsg])
//			}
//	}
//
//	implicit val cpPacketWriter: Writes[CPPacket] = new Writes[CPPacket] {
//    def writes(t: CPPacket) = Json.obj(
//      "kind" -> t.kind,
//      "event" -> t.event
//    )
//  }

  implicit val cpPacketReader: Reads[CPPacket] = 
    (__ \ "msg").read[String].flatMap { kind =>
      (kind match {
        case KIND_SEND_MSG => (__ \ "event").read[CPSendMsg]
        case KIND_RECEIVE_MSG => (__ \ "event").read[CPReceiveMsg]
        case KIND_REJECTED => (__ \ "event").read[CPRejected]
      }).map(event => CPPacket(kind, event))
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
