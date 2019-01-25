package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao.{MenuDAO, OptionDAO, PostDAO}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class AppController @Inject()(deadbolt: DeadboltActions,
															cc: ControllerComponents,
															postDAO: PostDAO,
															config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends AbstractController(cc)
		with I18nSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def index = deadbolt.WithAuthRequest()() { implicit request =>

		def default = future(Ok(views.html.app.index2()))

		optionDAO
			.getOptionByName(models.Options.INDEX_PAGE_ID)
			.flatMap(_.fold(default) { option =>
				option.toOptLong.fold(default) { pageId =>
					postDAO.findPostById(pageId).flatMap {
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
