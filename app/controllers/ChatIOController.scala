package controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao.{AccountDAO, ChatMsgDAO, MenuDAO, OptionDAO}
import models.{Session => _, _}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import security.AuthSupport
import services.InputSanitizer

import scala.concurrent.ExecutionContext

@Singleton
class ChatIOController @Inject()(deadbolt: DeadboltActions,
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

	def chatPage = deadbolt.WithAuthRequest()() { implicit request =>
		chatMsgDAO.getLast(5) flatMap { lastMsgs =>
			accountDAO.findAccountsByIds(lastMsgs.map(_.ownerId)) map { accounts =>

				val ncMsgs = lastMsgs.map { m =>
					NCMsgOut(accounts.find(_.id == m.ownerId).map(_.login).getOrElse("unknown"),
						m.msg,
						controllers.TimeConstants.prettyTime.format(new java.util.Date(System.currentTimeMillis())))
				}

				Ok(views.html.app.chatIO(ncMsgs))
			}
		}
	}

}
