package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.models.PatternType
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Permission, SNNAccountTypes}
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

	def removeSNAccount(snAccountId: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE), PatternType.REGEX)() { implicit request =>
		snAccountDAO.findSNAccountsById(snAccountId).flatMap(_.fold(future(NotFound("Sn account with id " + snAccountId + " not found!"))) { snAccount =>
			checkedOwner(snAccount.ownerId, Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE) {
				snAccountDAO.removeById(snAccountId) map { _ =>
					val url = request.session.get(AppConstants.RETURN_URL).getOrElse(routes.AccountsController.panelProfile(snAccount.ownerId).toString())
					Redirect(url).flashing("success" -> ("SN account successfully removed!"))
				}
			}
		})
	}

	def changeSNAccount(snAccountId: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE), PatternType.REGEX)() { implicit request =>
		snAccountDAO.findSNAccountsById(snAccountId).flatMap(_.fold(future(NotFound("Sn account with id " + snAccountId + " not found!"))) { snAccount =>
			checkedOwner(snAccount.ownerId, Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE) {
				future(Ok(views.html.admin.editSNAccount(snAccount.id, snAccountForm.fill(SNAccountData(snAccount.snType.toString, snAccount.login)))))
			}
		})
	}

	def updateSNAccount(snAccountId: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE), PatternType.REGEX)() { implicit request =>
		snAccountDAO.findSNAccountsById(snAccountId).flatMap(_.fold(future(NotFound("Sn account with id " + snAccountId + " not found!"))) { snAccount =>
			checkedOwner(snAccount.ownerId, Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE) {
				snAccountForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.editSNAccount(snAccount.id, formWithErrors))), { snAccountData =>
					snAccountData.getSNType.fold(future(NotFound("Such sn account type not found"))) { snAccountType =>
						snAccountDAO.snAccountExists(snAccount.ownerId, snAccountData.login, snAccountType) flatMap { exists =>
							if (exists)
								future(BadRequest(views.html.admin.editSNAccount(snAccount.id, snAccountForm.fill(snAccountData))).flashing("error" -> "SN account with such paramteres already exists!"))
							else
								snAccountDAO.updateSNAccount(snAccountId,
									snAccountData.login,
									snAccountType) map { _ =>
									Redirect(controllers.routes.AccountsController.panelProfile(snAccount.ownerId))
										.flashing("success" -> ("SN account successfully created!"))
								}
						}
					}
				})
			}
		})
	}

	def processCreateSNAccount(accountId: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE), PatternType.REGEX)() { implicit request =>
		checkedOwner(accountId, Permission.PERM__PROFILE_ANY_CHANGE, Permission.PERM__PROFILE_OWN_CHANGE) {
			snAccountForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createSNAccount(accountId, formWithErrors))), { snAccountData =>
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

}


