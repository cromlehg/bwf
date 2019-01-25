package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.Permission
import models.dao.{MenuDAO, OptionDAO}
import play.api.Configuration
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class MenuController @Inject()(cc: ControllerComponents,
															 deadbolt: DeadboltActions,
															 config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	def adminMenusListPage = deadbolt.Pattern(Permission.PERM__MENU_VIEW)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			menuDAO.menusListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { items =>
				Ok(views.html.admin.parts.menusListPage(items))
			}
		}))
	}

	def adminMenusListPagesCount = deadbolt.Pattern(Permission.PERM__MENU_VIEW)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			menuDAO.menusListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def adminMenus = deadbolt.Pattern(Permission.PERM__MENU_VIEW)() { implicit request =>
		future(Ok(views.html.admin.menus()))
	}

}

