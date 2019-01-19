package services

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MailVerifier @Inject()(config: Config, ws: WSClient)(implicit ec: ExecutionContext) {

  private val API_KEY = config.getString("mailverifier.apikey")
  private val API_URL = config.getString("mailverifier.apiurl")
  private val IS_ON = config.getBoolean("mailverifier.ison")

  def isValid(email: String): Future[Boolean] = {
    if (IS_ON) {
      ws.url(API_URL).addQueryStringParameters("key" -> API_KEY, "email" -> email).get() flatMap {
        response => {
          Logger.debug("Mail verifier response: " + response.body)
          (response.json \ "status").asOpt[String] match {
            case Some("Bad") => {
              Logger.debug("Email verification test for " + email + " failed!")
              Future.successful(false)
            }
            case Some("Ok") | Some("Unknown") => Future.successful(true)
            case _ => {
              val message = (response.json \ "Message").asOpt[String]
              throw new Exception(message.getOrElse("Could not get status from ws response"))
            }
          }
        }
      }
    } else {
      Logger.debug("Mail verifier is off. Mail verification test always return true!")
      Future.successful(true)
    }
  }

}