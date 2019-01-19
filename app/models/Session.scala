package models

import play.api.libs.json.{JsValue, Json}

case class Session(
                    val id: Long,
                    val userId: Long,
                    val ip: String,
                    val sessionKey: String,
                    val created: Long,
                    val expire: Long) extends TraitDateSupports {

  def toJson: JsValue = Json.obj()

}

object Session {

  val TOKEN = "TOKEN"

  def apply(
             id: Long,
             userId: Long,
             ip: String,
             sessionKey: String,
             created: Long,
             expire: Long) =
    new Session(
      id,
      userId,
      ip,
      sessionKey,
      created,
      expire)

}

    
