package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.models.PatternType
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.{Permission, SNNAccountTypes}
import models.dao._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class SNAccountsController @Inject()(cc: ControllerComponents,
																		 deadbolt: DeadboltActions,
																		 snAccountDAO: SNAccountDAO,
																		 config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class SNAccountData(val snType: String,
													 val login: String) {

		lazy val getSNType: Option[SNNAccountTypes.SNNAccountType] =
			SNNAccountTypes.valueOf(snType)

	}

	val snAccountForm = Form(
		mapping(
			"snType" -> nonEmptyText(3, 100),
			"login" -> nonEmptyText(3, 100))(SNAccountData.apply)(SNAccountData.unapply))

	def createSNAccount(accountId: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE), PatternType.REGEX)() { implicit request =>
		checkedOwner(accountId, Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE) {
			future(Ok(views.html.admin.createSNAccount(accountId, snAccountForm)))
		}
	}

	def processCreateSNAccount(accountId: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE), PatternType.REGEX)() { implicit request =>
		checkedOwner(accountId, Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE) {
			snAccountForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createSNAccount(accountId, formWithErrors))), { snAccountData =>
				println(snAccountData)
				snAccountData.getSNType.fold(future(NotFound("Such sn account type not found"))) { snAccountType =>
					snAccountDAO.snAccountExists(accountId, snAccountData.login, snAccountType) flatMap { exists =>
						if (exists)
							future(BadRequest(views.html.admin.createSNAccount(accountId, snAccountForm.fill(snAccountData))).flashing("error" -> "SN account with such paramteres already exists!"))
						else
							snAccountDAO.createSNAccount(
								accountId,
								snAccountData.login,
								snAccountType) map { snAccount =>
								Redirect(controllers.routes.AccountsController.panelProfile(accountId))
									.flashing("success" -> ("SN account successfully created!"))
							}
					}
				}
			})
		}
	}


	/*
    def processUpdateRole(id: Long) = deadbolt.Pattern(Permission.PERM__PERMISSIONS_CHANGE_ANYTIME)() { implicit request =>
      roleForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createRole(formWithErrors))), { roleData =>
        roleDAO.findRoleById(id) flatMap (_.fold(future(NotFound("Role not found"))) { role =>

          def updateRole =
            roleDAO.updateRole(
              role.id,
              roleData.getName,
              roleData.descr) map { updated =>
              if(updated)
                Redirect(controllers.routes.RolesController.viewRole(role.id))
                  .flashing("success" -> ("Role successfully created!"))
              else
                NotFound("Can't update role")
            }

          if(role.name == roleData.getName)
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
    }*/


}


