package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Permission, SystemUsers, PlatformError}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.mvc.ControllerComponents
import cats.data.EitherT
import cats.implicits._

import scala.concurrent.ExecutionContext


@Singleton
class UsersController @Inject()(cc: ControllerComponents,
																deadbolt: DeadboltActions,
																config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc, config) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class PlatformUserData(login: String,
															password: String,
															repassword: String)

	val puForm = Form(
		mapping(
			"login" -> nonEmptyText(minLength = 1, maxLength = 64)
				.verifying("login should be from 8 to 64 length of lowercase [a-z]", _.matches("[a-z0-9]{1,64}")),
			"password" -> nonEmptyText(minLength = 8, maxLength = 64),
			"repassword" -> nonEmptyText(minLength = 8, maxLength = 64)
		)(PlatformUserData.apply)(PlatformUserData.unapply))

	def systemUsers = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
    SystemUsers.list.map(_ match {
      case Right(t) => Ok(views.html.admin.systemUsers(t))
      case Left(pe) => BadRequest(pe.descr.getOrElse("Error"))
    })
	}

	def createPlatformUser = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.createPlatformUser(puForm)))
	}

	def processCreatePlatformUser = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		puForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createPlatformUser(formWithErrors))), { puData =>

			def err(msg: String) = {
				val formWE = puForm.fill(puData).withGlobalError(msg)
				future(BadRequest(views.html.admin.createPlatformUser(formWE)))
			}

			if (puData.password.equals(puData.repassword)) {

        val r = for {
          _ <- EitherT(SystemUsers.existsByLogin(puData.login)
                .map(_.flatMap(t => if (t) Left(PlatformError("User with login " + puData.login + " already exists in system!")) else Right(""))))
          _ <- EitherT(SystemUsers.createUser(puData.login, puData.password, operatorPassword))
          _ <- EitherT(SystemUsers.existsByLogin(puData.login)
                .map(_.flatMap(t => if (t) Right(puData.login) else Left(PlatformError("Can't create system user with login " + puData.login + " !")))))
          u <- EitherT(dap.platformUsers.createPlatformUser(puData.login, ac.actor.id))
        } yield u

        r.value.flatMap(_ match {
          case Right(user) =>
            future(Redirect(controllers.routes.UsersController.adminPlatformUsers)
                .flashing("success" -> ("User with login " + user + "\" has been created!")))
          case Left(pe) => err(pe.descr.getOrElse("Error"))
        })

      } else err("Passwords must be equals!")

		})
	}

	def adminPlatformUsers = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.platformUsers()))
	}

	def adminPlatformUsersListPage = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.platformUsers.platformUsersListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { options =>
				Ok(views.html.admin.parts.platformUsersListPage(options))
			}
		}))
	}

	def adminPlatformUsersListPagesCount = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.platformUsers.platformUsersListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

}

