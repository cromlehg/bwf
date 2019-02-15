package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.ChatProtocol.ChatMessageOut
import models.dao.{AccountDAO, ChatMsgDAO, MenuDAO, OptionDAO}
import models.{Session => _}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import security.AuthSupport
import services.InputSanitizer

import scala.concurrent.ExecutionContext

@Singleton
class ChatController @Inject()(deadbolt: DeadboltActions,
															 cc: ControllerComponents,
															 chatMsgDAO: ChatMsgDAO,
															 authSupport: AuthSupport,
															 accountDAO: AccountDAO,
															 inputSanitizer: InputSanitizer,
															 config: Configuration)(implicit ec: ExecutionContext,
																											mat: Materializer,
																											actorSystem: ActorSystem,
																											optionDAO: OptionDAO,
																											menuDAO: MenuDAO)
	extends AbstractController(cc)
		with I18nSupport
		with LoggerSupport {

  private val IS_SECURED = config.get[Boolean]("bwf.websockets.secured")

	def chatPage = deadbolt.WithAuthRequest()() { implicit request =>
		chatMsgDAO.getLast(AppConstants.CHAT_MSGS_LIMIT) flatMap { lastMsgs =>
			accountDAO.findAccountsByIds(lastMsgs.map(_.ownerId)) map { accounts =>

				val msgsToView = lastMsgs.map { m =>
					ChatMessageOut(accounts.find(_.id == m.ownerId).map(_.login).getOrElse("unknown"),
						m.msg,
						m.timestamp)
				}

				Ok(views.html.app.chat(msgsToView, AppConstants.CHAT_MSGS_LIMIT, IS_SECURED))
			}
		}
	}

}
