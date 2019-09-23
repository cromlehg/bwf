package controllers

import javax.inject.{Inject, Singleton}
import models.dao.DAOProvider
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommonAbstractController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends AbstractController(cc) with I18nSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def checkedOwner(targetOwnerId: Long, anyPermission: String, ownPermission: String)(f: => Future[Result])(implicit ac: AppContext): Future[Result] =
		if (ac.actor.containsPermission(anyPermission) || (ac.actor.containsPermission(ownPermission) && ac.actor.id == targetOwnerId))
			f
		else
			future(BadRequest("You are not authorized to this action!"))

	def errorRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		request.headers.get("referer")
			.fold {
				Redirect(call).flashing("error" -> (msg))
			} { url =>
				Redirect(url).flashing("error" -> (msg))
			}

	def asyncErrorRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		future(errorRedirect(msg, call))

	def successRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		request.headers.get("referer")
			.fold {
				Redirect(call).flashing("success" -> (msg))
			} { url =>
				Redirect(url).flashing("success" -> (msg))
			}

	def asyncSuccessRedirect[T](msg: String, call: Call = routes.AppController.index)(implicit request: Request[T]) =
		future(successRedirect(msg, call))

	def booleanOptionFold(name: String)(ifFalse: Future[Result])(ifTrue: Future[Result]): Future[Result] =
		dap.options.getOptionByName(name) flatMap {
			_.fold(future(BadRequest("Not found option " + name)))(option => if (option.toBoolean) ifTrue else ifFalse)
		}

}

