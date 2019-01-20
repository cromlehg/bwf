package models

import play.api.libs.json.{JsValue, Json}

case class Session(
                    val id: Long,
                    val userId: Long,
                    val ip: String,
										val userAgent: Option[String],
										val os: Option[String],
										val device: Option[String],
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
						 userAgent: Option[String],
						 os: Option[String],
						 device: Option[String],
						 sessionKey: String,
             created: Long,
             expire: Long) =
    new Session(
      id,
      userId,
      ip,
			userAgent,
			os,
			device,
			sessionKey,
      created,
      expire)

}


