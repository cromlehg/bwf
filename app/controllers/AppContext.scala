package controllers

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import be.objectify.deadbolt.scala.AuthenticatedRequest
import be.objectify.deadbolt.scala.models.Subject
import models.Account
import models.dao.MenuDAO
import models.dao.OptionDAO

case class AppContext(val authorizedOpt: Option[models.Account] = None, optionDAO: OptionDAO, menuDAO: MenuDAO) {

  import scala.concurrent.Future.{ successful => future }

  lazy val actor = authorizedOpt.get

  lazy val afterPageScript: String =
    Await.result(optionDAO
      .getOptionByName(models.Options.AFTER_PAGE_SCRIPT)
      .map(_.map(_.value).getOrElse("")), 5.seconds)

  lazy val mainMenu: Option[models.Menu] = {
    val r = optionDAO.getOptionByName(models.Options.MAIN_MENU_ID)
      .flatMap(_ match {
        case Some(option) =>
          option.toOptInt match {
            case Some(menuId) => menuDAO.getMenuById(menuId)
            case _ => future(None)
          }
        case _ => future(None)
      })
    Await.result(r, 5.seconds)
  }

}

object AppContextObj {

  def apply(
    subject: Option[Subject],
    optionDAO: OptionDAO,
    menuDAO: MenuDAO): AppContext =
    new AppContext(
      subject.map(_.asInstanceOf[Account]),
      optionDAO,
      menuDAO)

}

object AuthRequestToAppContext {

  implicit def ac(implicit
    request: AuthenticatedRequest[_],
    optionDAO: models.dao.OptionDAO,
    menuDAO: MenuDAO) =
    AppContextObj(
      request.subject,
      optionDAO,
      menuDAO)

}
