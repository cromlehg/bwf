package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.Permission
import models.dao._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class PermissionsController @Inject()(cc: ControllerComponents,
																			deadbolt: DeadboltActions,
																			permissionDAO: PermissionDAO,
																			config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class PermissionData(val name: String,
														val descr: Option[String]) {

		def getName = name.trim.toLowerCase

	}

	val permissionForm = Form(
		mapping(
			"name" -> nonEmptyText(3, 100),
			"descr" -> optional(text))(PermissionData.apply)(PermissionData.unapply))

	def editPermission(id: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		permissionDAO.findPermissionById(id) map (_.fold(NotFound("Permission not found")) { t =>
			Ok(views.html.admin.editPermission(permissionForm.fill(PermissionData(t.value, t.descr)), t.id))
		})
	}

	def processUpdatePermission(id: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		permissionForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createPermission(formWithErrors))), { permissionData =>
			permissionDAO.findPermissionById(id) flatMap (_.fold(future(NotFound("Role not found"))) { permission =>

				def updatePermission =
					permissionDAO.updatePermission(
						permission.id,
						permissionData.getName,
						permissionData.descr) map { updated =>
						if (updated)
							Redirect(controllers.routes.PermissionsController.adminPermissions)
								.flashing("success" -> ("Permission successfully created!"))
						else
							NotFound("Can't update permission")
					}

				if (permission.value == permissionData.getName)
					updatePermission
				else
					permissionDAO.permissionExistsByValue(permissionData.getName) flatMap { exists =>
						if (exists)
							future(BadRequest(views.html.admin.editPermission(permissionForm.fill(permissionData), permission.id)).flashing("error" -> "Permission with specified name already exists"))
						else
							updatePermission
					}

			})
		})
	}

	def createPermission = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		future(Ok(views.html.admin.createPermission(permissionForm)))
	}

	def processCreatePermission = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		permissionForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createPermission(formWithErrors))), { permissionData =>
			permissionDAO.permissionExistsByValue(permissionData.name) flatMap { exists =>
				if (exists)
					future(BadRequest(views.html.admin.createPermission(permissionForm.fill(permissionData))).flashing("error" -> "Permission with specified name already exists"))
				else
					permissionDAO.createPermission(
						permissionData.getName,
						permissionData.descr) map { role =>
						Redirect(controllers.routes.PermissionsController.adminPermissions)
							.flashing("success" -> ("Permission successfully created!"))
					}
			}
		})
	}

	def adminPermissionsListPage = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			permissionDAO.permissionsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { items =>
				Ok(views.html.admin.parts.permissionsListPage(items))
			}
		}))
	}

	def adminPermissionsListPagesCount = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			permissionDAO.permissionsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def adminPermissions = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		future(Ok(views.html.admin.permissions()))
	}

}

