package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Permission, SystemUsers}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, longNumber, text, optional}
import play.api.mvc.ControllerComponents
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, of}
import play.api.data.format.Formats._
import play.api.data.format.Formatter

import scala.concurrent.ExecutionContext


@Singleton
class ProjectsController @Inject()(cc: ControllerComponents,
																	 deadbolt: DeadboltActions,
																	 config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	implicit object UrlFormatter extends Formatter[java.net.URL] {
		override val format = Some(("format.url", Nil))

		override def bind(key: String, data: Map[String, String]) = parsing(new java.net.URL(_), "error.url", Nil)(key, data)

		override def unbind(key: String, value: java.net.URL) = Map(key -> value.toString)
	}


	case class PlatformProjectData(name: String,
																 userId: Long,
																 gitURL: java.net.URL,
																 gtiLogin: String,
																 gitPwd: String,
																 //																 dbName: String,
																 //																 dbUser: String,
																 //																 dbPass: String,
																 descr: Option[String])

	val ppForm = Form(
		mapping(
			"name" -> nonEmptyText(minLength = 1, maxLength = 100),
			"userId" -> longNumber(min = 1),

			"gitURL" -> of[java.net.URL],
			"gitLogin" -> nonEmptyText(minLength = 1, maxLength = 100),
			"gitPwd" -> nonEmptyText(minLength = 8, maxLength = 100),
			"descr" -> optional(text)

			//			"dbName" -> nonEmptyText(minLength = 1, maxLength = 100),
			//			"dbUser" -> nonEmptyText(minLength = 1, maxLength = 100),
			//			"dbPass" -> nonEmptyText(minLength = 8, maxLength = 100),

		)(PlatformProjectData.apply)(PlatformProjectData.unapply))


	def createPlatformProject = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(NotImplemented)
		//future(Ok(views.html.admin.createPlatformProject(puForm)))
	}

	def processCreatePlatformProject = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(NotImplemented)
//		ppForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createPlatformProject(formWithErrors))), { ppData =>
//
//			def err(msg: String) = {
//				val formWE = ppForm.fill(ppData).withGlobalError(msg)
//				future(BadRequest(views.html.admin.createPlatformProject(formWE)))
//			}
//
//			if (ppData.password.equals(puData.repassword)) {
//				if (SystemUsers.existsByLogin(puData.login)) {
//					err("User with login " + puData.login + " already exists in system!")
//				} else {
//					SystemUsers.createUser(puData.login,
//						puData.password,
//						operatorPassword)
//
//					if (SystemUsers.existsByLogin(puData.login)) {
//						dap.platformUsers.createPlatformUser(puData.login, ac.actor.id).flatMap(_ match {
//							case Right(user) =>
//								future(Redirect(controllers.routes.UsersController.adminPlatformUsers)
//									.flashing("success" -> ("User with login " + user.login + "\" has been created!")))
//							case Left(msg) => err(msg)
//						})
//					} else {
//						err("Can't create system user with login " + puData.login + " !")
//					}
//				}
//			} else {
//				err("Passwords must be equals!")
//			}
//
//
//		})

	}

	def adminPlatformProjects = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.platformProjects()))
	}

	def adminPlatformProjectsListPage = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.platformProjects.platformProjectsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { options =>
				Ok(views.html.admin.parts.platformProjectsListPage(options))
			}
		}))
	}

	def adminPlatformProjectsListPagesCount = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.platformProjects.platformProjectsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

}

