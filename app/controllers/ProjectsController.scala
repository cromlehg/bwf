package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import cats.data.EitherT
import cats.implicits._
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models._
import models.dao._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, of, optional, text}
import play.api.data.format.Formats._
import play.api.data.format.Formatter
import play.api.mvc.ControllerComponents
import services.SystemHelper

import scala.concurrent.ExecutionContext
import scala.util.Random


@Singleton
class ProjectsController @Inject()(cc: ControllerComponents,
																	 deadbolt: DeadboltActions,
																	 config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc, config) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	implicit object UrlFormatter extends Formatter[java.net.URL] {
		override val format = Some(("format.url", Nil))

		override def bind(key: String, data: Map[String, String]) = parsing(new java.net.URL(_), "error.url", Nil)(key, data)

		override def unbind(key: String, value: java.net.URL) = Map(key -> value.toString)
	}


	case class PlatformProjectData(name: String,
																 port: Long,
																 user: String,
																 gitURL: java.net.URL,
																 gitLogin: Option[String],
																 gitPwd: Option[String],
																 descr: Option[String])

	val ppForm = Form(
		mapping(
			"name" -> nonEmptyText(minLength = 1, maxLength = 100),
			"port" -> longNumber(1, 99999),
			"user" -> nonEmptyText(minLength = 1, maxLength = 100),
			"git_url" -> of[java.net.URL],
			"git_user" -> optional(text(minLength = 1, maxLength = 100)),
			"git_pwd" -> optional(text(minLength = 8, maxLength = 100)),
			"descr" -> optional(text)
		)(PlatformProjectData.apply)(PlatformProjectData.unapply))


	def createPlatformProject = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.createPlatformProject(ppForm)))
	}

	def processCreatePlatformProject = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		ppForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createPlatformProject(formWithErrors))), { ppData =>

			def err(msg: String) = {
				val formWE = ppForm.fill(ppData).withGlobalError(msg)
				future(BadRequest(views.html.admin.createPlatformProject(formWE)))
			}

			// FIXME: 
			val localDBPass = "sguyqwfdyufqwysf1273192t"

			val r = for {
				u <- EitherT(dap.platformUsers.findPlatformUserOptByLogin(ppData.user).map(_.toRight(PlatformError("Platform user not found!"))))
				_ <- EitherT(SystemUsers.existsByLogin(ppData.user)
					.map(_.flatMap(t => if (t) Right(ppData.user) else Left(PlatformError("Can't find system user with login " + ppData.user + " !")))))
				// Создание путей для bwf папки в домашней директории пользователя
				_ <- EitherT(SystemHelper.createPathRecBySUDO(PlatformProject.bwfPath(ppData.user), operatorPassword))
				_ <- EitherT(SystemHelper.chGroupBySUDO(PlatformProject.bwfPath(ppData.user), operatorPassword, PlatformProject.usersGroup, false))
				_ <- EitherT(SystemHelper.chOwnerBySUDO(PlatformProject.bwfPath(ppData.user), operatorPassword, ppData.user, false))
				cp <- EitherT(dap.platformProjects.createPlatformProject(ppData.name,
					u.id,
					u.login,
					ppData.gitURL.toString,
					ppData.gitLogin,
					ppData.gitPwd,
					ppData.port,
					ppData.descr))
				// Создание путей для окружения проекта - папка проекта
				_ <- EitherT(SystemHelper.createPathRecBySUDO(PlatformProject.ppPath(ppData.user, cp.id), operatorPassword))
				_ <- EitherT(SystemHelper.chGroupBySUDO(PlatformProject.ppPath(ppData.user, cp.id), operatorPassword, PlatformProject.usersGroup, false))
				_ <- EitherT(SystemHelper.chOwnerBySUDO(PlatformProject.ppPath(ppData.user, cp.id), operatorPassword, ppData.user, false))
				// создание базы данных
				_ <- EitherT(PlatformProject.createDB(dbUser, dbPasswd, PlatformProject.dbName(cp.id), PlatformProject.dbUser(cp.id), localDBPass))
				// обновление данных о БД
				_ <- EitherT(dap.platformProjects.updatePlatformDBProps(cp.id, Some(PlatformProject.dbName(cp.id)), Some(PlatformProject.dbUser(cp.id)), Some(localDBPass)))
				// обновлени состояния до готовности к подготовка (тогда планировщик начнет готовить проект)
				up <- EitherT(dap.platformProjects.updatePlatformProjectStatus(cp.id, PlatformProjectStatuses.PREPARE))
			} yield up

			r.value.flatMap(_ match {
				case Right(prj) =>
					future(Redirect(controllers.routes.ProjectsController.adminPlatformProjects)
						.flashing("success" -> ("Project " + prj.name + "\" has been created!")))
				case Left(pe) => err(pe.descr.getOrElse("Error"))
			})

		})

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

