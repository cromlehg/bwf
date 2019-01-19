package controllers

import javax.inject.{Inject, Singleton}
import models.dao.OptionDAO
import play.api.i18n.I18nSupport
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommonAbstractController @Inject()(
                                          optionDAO: OptionDAO,
                                          cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) with I18nSupport with LoggerSupport {

  import scala.concurrent.Future.{successful => future}

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
    optionDAO.getOptionByName(name) flatMap {
      _.fold(future(BadRequest("Not found option " + name)))(option => if (option.toBoolean) ifTrue else ifFalse)
    }

}

