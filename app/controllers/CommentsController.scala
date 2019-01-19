package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.models.PatternType
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.Permission
import models.dao._
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
class CommentsController @Inject()(
																		cc: ControllerComponents,
																		deadbolt: DeadboltActions,
																		accountDAO: AccountDAO,
																		commentDAO: CommentDAO,
																		postDAO: PostDAO,
																		config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
	extends CommonAbstractController(optionDAO, cc) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class CommentData(val content: String)

	val permissionForm = Form(
		mapping(
			"name" -> nonEmptyText(3, 100))(CommentData.apply)(CommentData.unapply))

	def adminCommentsListPage = deadbolt.Pattern(Permission.OR(Permission.PERM__COMMENTS_CHANGE_ANYTIME, Permission.PERM__COMMENTS_CHANGE_OWN), PatternType.REGEX)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			commentDAO.commentsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt,
				None,
				if (ac.actor.containsPermission(Permission.PERM__COMMENTS_CHANGE_ANYTIME))
					None
				else
					Some(ac.actor.id)
			)(postDAO) map { items =>
				Ok(views.html.admin.parts.commentsListPage(items))
			}
		}))
	}

	def adminCommentsListPagesCount = deadbolt.Pattern(Permission.OR(Permission.PERM__COMMENTS_CHANGE_ANYTIME, Permission.PERM__COMMENTS_CHANGE_OWN), PatternType.REGEX)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			commentDAO.commentsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt,
				None,
				if (ac.actor.containsPermission(Permission.PERM__COMMENTS_CHANGE_ANYTIME))
					None
				else
					Some(ac.actor.id)) map { count => Ok(count.toString) }
		})
	}

	def adminComments = deadbolt.Pattern(Permission.OR(Permission.PERM__COMMENTS_CHANGE_ANYTIME, Permission.PERM__COMMENTS_CHANGE_OWN), PatternType.REGEX)() { implicit request =>
		future(Ok(views.html.admin.comments()))
	}

}

