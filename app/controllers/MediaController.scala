package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao.DAOProvider
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext
import scala.reflect.io.{File, Path}

@Singleton
class MediaController @Inject()(cc: ControllerComponents,
																deadbolt: DeadboltActions,
																config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends AbstractController(cc) with I18nSupport with LoggerSupport with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	val path = config.get[String]("bwf.media.path")

	def upload = deadbolt.SubjectPresent()(parse.multipartFormData(maxLength = 100 * 1024)) { implicit request =>
		request.body.file("file").map { file =>

			def saveFile(extension: String) = {
				val relativeDirPart = ac.actor.login + File.separator + java.time.LocalDateTime.now.atZone(java.time.ZoneId.of("GMT")).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
				val absDirPart = path + File.separator + relativeDirPart
				Path(absDirPart).createDirectory(failIfExists = false)
				val filename = System.currentTimeMillis.toString + "." + extension
				val absFilePath = absDirPart + File.separator + filename
				val absFileURL = "/app/media" + File.separator + relativeDirPart + File.separator + filename

				file.ref.moveTo(java.nio.file.Paths.get(absFilePath), replace = true)
				future(Ok("{\"location\":\"" + absFileURL + "\"}"))
			}

			file.contentType match {
				case Some("image/jpg") => saveFile("jpg")
				case Some("image/jpeg") => saveFile("jpg")
				case t => future(BadRequest("Wrong content type: " + t))
			}

		}.getOrElse {
			future(BadRequest("Missing file"))
		}
	}

	def media(file: String): Action[AnyContent] = Action { implicit request =>
		val absolutePath = path + File.separator + file
		Ok.sendFile(new java.io.File(absolutePath))
	}

}

