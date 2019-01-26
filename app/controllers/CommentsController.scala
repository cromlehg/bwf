package controllers

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.models.PatternType
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{CommentContentTypes, CommentStatusTypes, CommentTargetTypes, Permission}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.mvc.ControllerComponents
import security.HandlerKeys

import scala.concurrent.ExecutionContext
import scala.language.reflectiveCalls

@Singleton
class CommentsController @Inject()(
																		cc: ControllerComponents,
																		handlerCache: HandlerCache,
																		deadbolt: DeadboltActions,
																		actionBuilder: ActionBuilders,
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

	// curl -X POST http://localhost:9000/app/posts/post/1/comments
	//	def postComments(postId: Long) = deadbolt.WithAuthRequest()() { implicit request =>
	//		commentDAO.allCommentsForTargetWithAccounts(postId, CommentTargetTypes.POST)(accountDAO) map { comments =>
	//				import models.Comment.commentsTreeBuilder
	//  			import models.Comment.commentsTreeWrites
	//				Ok(Json.toJson(comments.buildTree))
	//		}
	//	}


	//	// needs to inject actionBuilder: ActionBuilders
	//	def permittedFunctionA = actionBuilder
	//		.PatternAction(Permission.OR(Permission.PERM__COMMENTS_CREATE_ANYTIME, Permission.PERM__COMMENTS_CREATE_CONDITIONAL), PatternType.REGEX)
	//		.key(HandlerKeys.jsonHandler)(parse.json) { implicit request =>
	//		future {
	//			Ok("")
	//		}
	//	}
	//
	//	// needs to inject deadbolt: HandlerCache,
	//	def permittedFunctionB =  deadbolt.Pattern(
	//		Permission.OR(Permission.PERM__COMMENTS_CREATE_ANYTIME, Permission.PERM__COMMENTS_CREATE_CONDITIONAL),
	//		PatternType.REGEX, handler = handlerCache.apply(HandlerKeys.jsonHandler))(parse.json) { implicit request =>
	//			future {
	//				Ok("")
	//			}
	//		}

	def createComment = deadbolt.Pattern(
		Permission.OR(Permission.PERM__COMMENTS_CREATE_ANYTIME, Permission.PERM__COMMENTS_CREATE_CONDITIONAL),
		PatternType.REGEX, handler = handlerCache.apply(HandlerKeys.jsonHandler))(parse.json) { implicit request =>
		fieldString("content")(content => fieldLong("post_id")(postId => fieldLongOpt("parent_id") { parentIdOpt =>
			postDAO.existsPostById(postId) flatMap { postExists =>
				if (postExists) {

					def processCreateComment() =
						commentDAO.createComment(ac.actor.id,
							CommentTargetTypes.POST,
							postId,
							parentIdOpt,
							CommentContentTypes.TEXT,
							content: String,
							CommentStatusTypes.NORMAL) map { comment =>
							Ok(views.html.app.common.comment(comment.copy(owner = Some(ac.actor))))
						}

					parentIdOpt.fold {
						processCreateComment()
					} { parentId =>
						commentDAO.existsCommentById(parentId) flatMap { commentExists =>
							if (commentExists)
								processCreateComment()
							else
								future(NotFound("Comment with id " + parentId + " specified as parent not found!"))
						}
					}


				} else
					future(NotFound("Post " + postId + " not found!"))

			}
		}))
	}

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

