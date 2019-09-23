package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao.DAOProvider
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class AppController @Inject()(
	deadbolt: DeadboltActions,
	cc: ControllerComponents,
	config: Configuration
)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends AbstractController(cc)
	with I18nSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def index = deadbolt.WithAuthRequest()() { implicit request =>

		def default = future(Ok(views.html.app.index2()))

		dap.options
			.getOptionByName(models.Options.INDEX_PAGE_ID)
			.flatMap(_.fold(default) { option =>
				option.toOptLong.fold(default) { pageId =>
					dap.posts.findPostById(pageId).flatMap {
						_.fold(default) { page =>
							future(Ok(views.html.app.viewPage(page)))
						}
					}
				}
			})

	}

	def panel = deadbolt.SubjectPresent()() { implicit request =>
		future(Ok(views.html.admin.panel()))
	}

}
