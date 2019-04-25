package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import javax.inject.{Inject, Singleton}
import models.dao._
import controllers.AuthRequestToAppContext.ac
import models.{Permission, PermissionTargetTypes}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, optional, seq, text}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class RolesController @Inject()(cc: ControllerComponents,
																deadbolt: DeadboltActions,
																config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class RoleData(val name: String,
											val descr: Option[String]) {

		def getName = name.trim.toLowerCase

	}

	case class RolePermissionsData(permissions: Seq[Long])

	val roleForm = Form(
		mapping(
			"name" -> nonEmptyText(3, 100),
			"descr" -> optional(text))(RoleData.apply)(RoleData.unapply))

	val rolePermissionsForm = Form(
		mapping(
			"permissions" -> seq(longNumber)
		)(RolePermissionsData.apply)(RolePermissionsData.unapply)
	)

	def editRole(id: Long) = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		dap.roles.findRoleById(id) map (_.fold(NotFound("Role not found")) { t =>
			Ok(views.html.admin.editRole(roleForm.fill(RoleData(t.name, t.descr)), t.id))
		})
	}

	def processUpdateRole(id: Long) = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		roleForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createRole(formWithErrors))), { roleData =>
			dap.roles.findRoleById(id) flatMap (_.fold(future(NotFound("Role not found"))) { role =>

				def updateRole =
					dap.roles.updateRole(
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
					dap.roles.roleExistsByName(roleData.getName) flatMap { exists =>
						if (exists)
							future(BadRequest(views.html.admin.editRole(roleForm.fill(roleData), role.id)).flashing("error" -> "Role with specified name already exists"))
						else
							updateRole
					}

			})
		})
	}

	def processAddRolePermissions(id: Long) = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		rolePermissionsForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createRole(formWithErrors))), { rolePermissionsData =>
			dap.roles.findRoleById(id) flatMap (_.fold(future(NotFound("Role not found"))) { role =>
				dap.permissions.assignPermissionsToTargetIfNotAssigned(rolePermissionsData.permissions, role.id, PermissionTargetTypes.ROLE) map { _ =>
					Redirect(controllers.routes.RolesController.viewRole(role.id))
						.flashing("success" -> "Role permissions successfully added!")
				}
			})
		})
	}


	def createRole = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.createRole(roleForm)))
	}

	def processCreateRole = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		roleForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createRole(formWithErrors))), { roleData =>
			dap.roles.roleExistsByName(roleData.name) flatMap { exists =>
				if (exists)
					future(BadRequest(views.html.admin.createRole(roleForm.fill(roleData))).flashing("error" -> "Role with specified name already exists"))
				else
					dap.roles.createRole(
						roleData.getName,
						roleData.descr) map { role =>
						Redirect(controllers.routes.RolesController.viewRole(role.id))
							.flashing("success" -> ("Role successfully created!"))
					}
			}
		})
	}

	def adminRolesListPage = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.roles.rolesListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { items =>
				Ok(views.html.admin.parts.rolesListPage(items))
			}
		}))
	}

	def adminRolesListPagesCount = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.roles.rolesListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def adminRoles = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.roles()))
	}

	def viewRole(id: Long) = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		dap.permissions.permissionsList flatMap { permissions =>
			dap.roles.findRoleById(id) map (_.fold(NotFound("Role not found"))(r => Ok(views.html.admin.viewRole(r, rolePermissionsForm, permissions))))
		}
	}

	def adminRolePermissionsListPage(roleId: Long) = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.permissions.permissionsListPageByTarget(
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

	def adminRolePermissionsListPagesCount(roleId: Long) = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.permissions.permissionsListPagesCountByTarget(
				roleId,
				PermissionTargetTypes.ROLE,
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

}

