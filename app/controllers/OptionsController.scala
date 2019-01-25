package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.Permission
import models.dao.{MenuDAO, OptionDAO}
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class OptionsController @Inject()(cc: ControllerComponents,
																	deadbolt: DeadboltActions,
																	config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends AbstractController(cc) with I18nSupport with JSONSupport with LoggerSupport {

	import scala.concurrent.Future.{successful => future}

	def switchBooleanOption = deadbolt.Pattern(Permission.PERM__OPTIONS_EDIT)(parse.json) { implicit request =>
		fieldString("name")(name => optionDAO.getOptionByName(name)
			.flatMap(_.fold(future(NotFound("Option with name \"" + name + "\" - not found!"))) { option =>
				if (option.ttype != models.Options.TYPE_BOOLEAN) future(BadRequest("Option must be boolean to switch")) else
					optionDAO.updateOptionByName(name, if (option.toBoolean) "false" else "true") map {
						_.fold(BadRequest("Can't update option"))(t => Ok(t.toBoolean.toString))
					}
			}))
	}

	//def adminOptions = deadbolt.Restrict(List(Array(Role.ROLE_ADMIN), Array(Role.ROLE_EDITOR), Array(Role.ROLE_WRITER)))() { implicit request =>
	def adminOptions = deadbolt.Pattern(Permission.PERM__OPTIONS_LIST_VIEW)() { implicit request =>
		future(Ok(views.html.admin.options()))
	}

	def adminOptionsListPage = deadbolt.Pattern(Permission.PERM__OPTIONS_LIST_VIEW)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			optionDAO.optionsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { options =>
				Ok(views.html.admin.parts.optionsListPage(options))
			}
		}))
	}

	def adminOptionsListPagesCount = deadbolt.Pattern(Permission.PERM__OPTIONS_LIST_VIEW)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			optionDAO.optionsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

}

