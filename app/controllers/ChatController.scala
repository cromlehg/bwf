package controllers

import java.net.URI

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Source}
import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao.{ChatMsgDAO, MenuDAO, OptionDAO}
import models.{NCMsg, NCMsgIn, NCMsgOut, Session}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents, _}
import security.AuthSupport
import services.InputSanitizer

import scala.concurrent.ExecutionContext

@Singleton
class ChatController @Inject()(deadbolt: DeadboltActions,
															 cc: ControllerComponents,
															 chatMsgDAO: ChatMsgDAO,
															 authSupport: AuthSupport,
															 inputSanitizer: InputSanitizer,
															 config: Configuration)(implicit ec: ExecutionContext,
																											mat: Materializer,
																											actorSystem: ActorSystem,
																											optionDAO: OptionDAO,
																											menuDAO: MenuDAO)
	extends AbstractController(cc)
		with I18nSupport
		with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	// chat room many clients -> merge hub -> broadcasthub -> many clients
	private val (chatSink, chatSource) = {
		// Don't log MergeHub$ProducerFailed as error if the client disconnects.
		// recoverWithRetries -1 is essentially "recoverWith"
		val source = MergeHub.source[NCMsgIn]
			.map(t => t.copy(msg = inputSanitizer.sanitize(t.msg)))
			.map { t =>
				NCMsgOut("Alex Web",
					t.msg,
					controllers.TimeConstants.prettyTime.format(new java.util.Date(System.currentTimeMillis())))
			}
			.recoverWithRetries(-1, { case _: Exception ⇒ Source.empty })

		val sink = BroadcastHub.sink[NCMsgOut]
		source.toMat(sink)(Keep.both).run()
	}

	private val userFlow: Flow[NCMsgIn, NCMsgOut, _] = {
		Flow.fromSinkAndSource(chatSink, chatSource)
	}

	def chatPage = deadbolt.WithAuthRequest()() { implicit request =>
		val webSocketUrl = routes.ChatController.chat().webSocketURL()
		future(Ok(views.html.app.chat(webSocketUrl)))
	}

	def chat(): WebSocket = {

		import NCMsg.messageFlowTransformer

		// not authorized
		WebSocket.acceptOrResult[NCMsgIn, NCMsgOut] {
			case request if sameOriginCheck(request) =>
								future(userFlow).map { flow =>
									Right(flow)
								}.recover {
									case e: Exception =>
										val msg = "Cannot create websocket"
										logger.error(msg, e)
										val result = InternalServerError(msg)
										Left(result)
								}

			case rejected =>
				logger.error(s"Request ${rejected} failed same origin check")
				future {
					Left(Forbidden("forbidden"))
				}
		}

//		WebSocket.acceptOrResult[NCMsgIn, NCMsgOut] {
//			case request if sameOriginCheck(request) =>
//				request.session.get(Session.TOKEN) match {
//					case Some(token) =>
//						val sessionKey = new String(java.util.Base64.getDecoder.decode(token))
//						authSupport.getAccount(sessionKey, request.remoteAddress) flatMap {
//							case Some(account)=>
//								future(userFlow).map { flow =>
//									Right(flow)
//								}.recover {
//									case e: Exception =>
//										val msg = "Cannot create websocket"
//										logger.error(msg, e)
//										val result = InternalServerError(msg)
//										Left(result)
//								}
//							case _ => future(Left(Forbidden))
//						}
//					case _ =>
//						future(Left(Forbidden))
//				}
//
//			case rejected =>
//				logger.error(s"Request ${rejected} failed same origin check")
//				future {
//					Left(Forbidden("forbidden"))
//				}
//		}
	}

	/**
		* Checks that the WebSocket comes from the same origin.  This is necessary to protect
		* against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
		*
		* See https://tools.ietf.org/html/rfc6455#section-1.3 and
		* http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
		*/
	private def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
		// The Origin header is the domain the request originates from.
		// https://tools.ietf.org/html/rfc6454#section-7
		logger.debug("Checking the ORIGIN ")

		rh.headers.get("Origin") match {
			case Some(originValue) if originMatches(originValue) =>
				logger.debug(s"originCheck: originValue = $originValue")
				true

			case Some(badOrigin) =>
				logger.error(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
				false

			case None =>
				logger.error("originCheck: rejecting request because no Origin header found")
				false
		}
	}

	/**
		* Returns true if the value of the Origin header contains an acceptable value.
		*/
	private def originMatches(origin: String): Boolean = {
		try {
			val url = new URI(origin)
			url.getHost == "localhost" &&
				(url.getPort match {
					case 9000 | 19001 => true;
					case _ => false
				})
		} catch {
			case e: Exception => false
		}
	}

}
