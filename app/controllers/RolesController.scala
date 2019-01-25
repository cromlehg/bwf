package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Permission, PermissionTargetTypes}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class RolesController @Inject()(cc: ControllerComponents,
																deadbolt: DeadboltActions,
																roleDAO: RoleDAO,
																permissionDAO: PermissionDAO,
																config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class RoleData(
											 val name: String,
											 val descr: Option[String]) {

		def getName = name.trim.toLowerCase

	}

	val roleForm = Form(
		mapping(
			"name" -> nonEmptyText(3, 100),
			"descr" -> optional(text))(RoleData.apply)(RoleData.unapply))

	def editRole(id: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		roleDAO.findRoleById(id) map (_.fold(NotFound("Role not found")) { t =>
			Ok(views.html.admin.editRole(roleForm.fill(RoleData(t.name, t.descr)), t.id))
		})
	}

	def processUpdateRole(id: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		roleForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createRole(formWithErrors))), { roleData =>
			roleDAO.findRoleById(id) flatMap (_.fold(future(NotFound("Role not found"))) { role =>

				def updateRole =
					roleDAO.updateRole(
						role.id,
						roleData.getName,
						roleData.descr) map { updated =>
						if (updated)
							Redirect(controllers.routes.RolesController.viewRole(role.id))
								.flashing("success" -> ("Role successfully created!"))
						else
							NotFound("Can't update role")
					}

				if (role.name == roleData.getName)
					updateRole
				else
					roleDAO.roleExistsByName(roleData.getName) flatMap { exists =>
						if (exists)
							future(BadRequest(views.html.admin.editRole(roleForm.fill(roleData), role.id)).flashing("error" -> "Role with specified name already exists"))
						else
							updateRole
					}

			})
		})
	}

	def createRole = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		future(Ok(views.html.admin.createRole(roleForm)))
	}

	def processCreateRole = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		roleForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createRole(formWithErrors))), { roleData =>
			roleDAO.roleExistsByName(roleData.name) flatMap { exists =>
				if (exists)
					future(BadRequest(views.html.admin.createRole(roleForm.fill(roleData))).flashing("error" -> "Role with specified name already exists"))
				else
					roleDAO.createRole(
						roleData.getName,
						roleData.descr) map { role =>
						Redirect(controllers.routes.RolesController.viewRole(role.id))
							.flashing("success" -> ("Role successfully created!"))
					}
			}
		})
	}

	def adminRolesListPage = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			roleDAO.rolesListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { items =>
				Ok(views.html.admin.parts.rolesListPage(items))
			}
		}))
	}

	def adminRolesListPagesCount = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			roleDAO.rolesListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def adminRoles = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		future(Ok(views.html.admin.roles()))
	}

	def viewRole(id: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
		roleDAO.findRoleById(id) map (_.fold(NotFound("Role not found"))(r => Ok(views.html.admin.viewRole(r))))
	}

	def adminRolePermissionsListPage(roleId: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			permissionDAO.permissionsListPageByTarget(
				roleId,
				PermissionTargetTypes.ROLE,
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { items =>
				Ok(views.html.admin.parts.permissionsListPage(items)) // permissionDAO
			}
		}))
	}

	def adminRolePermissionsListPagesCount(roleId: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			permissionDAO.permissionsListPagesCountByTarget(
				roleId,
				PermissionTargetTypes.ROLE,
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

}

