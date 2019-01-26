package security

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DynamicResourceHandler}
import controllers.AppConstants
import javax.inject.{Inject, Singleton}
import models.dao.RoleDAO
import play.api.mvc.{Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class BaseHandler @Inject()(authSupport: AuthSupport, roleDAO: RoleDAO)(dynamicResourceHandler: Option[DynamicResourceHandler] = None)
	extends AbstractWithSubjectHandler(authSupport, roleDAO, dynamicResourceHandler) {

	override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
		getSubject(request).map { maybeSubject =>
			maybeSubject match {
				case Some(_) =>
					request.headers.get(AppConstants.RETURN_URL)
						.fold {
							Results.Redirect(controllers.routes.AccountsController.denied())
						} { url =>
							Results.Redirect(url).flashing("error" -> "You have no permission!")
						}
				case _ =>
					Results.Redirect(controllers.routes.AccountsController.login)
						.withSession(request.session + AppConstants.RETURN_URL -> request.uri)
			}
		}


}
