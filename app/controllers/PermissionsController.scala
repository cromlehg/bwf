package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import models.Permission
import models.dao._
import controllers.AuthRequestToAppContext.ac
import play.api.Configuration
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class PermissionsController @Inject()(cc: ControllerComponents,
																			deadbolt: DeadboltActions,
																			config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	def adminPermissionsListPage = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.permissions.permissionsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { items =>
				Ok(views.html.admin.parts.permissionsListPage(items))
			}
		}))
	}

	def adminPermissionsListPagesCount = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.permissions.permissionsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def adminPermissions = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.permissions()))
	}

}

