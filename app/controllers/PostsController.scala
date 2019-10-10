package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.models.PatternType
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Options, Permission, PostTypes}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.mvc.{ControllerComponents, Flash, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostsController @Inject()(cc: ControllerComponents,
																deadbolt: DeadboltActions,
																config: Configuration
															 )(implicit ec: ExecutionContext, dap: DAOProvider)
	extends CommonAbstractController(cc)
		with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class PostData(title: String,
											tags: Option[String],
											content: String,
											metaTitle: Option[String],
											metaDescr: Option[String],
											metaKeywords: Option[String],
											postType: String
										 ) {

		val getTags: Seq[String] =
			tags.fold(Seq.empty[String])(_.split(",").map(_.trim.toLowerCase).filter(_.nonEmpty))

		def getPostType = PostTypes.idByStr(postType).get

	}

	val postForm = Form(
		mapping(
			"title" -> nonEmptyText(3, AppConstants.META_SIZE),
			"tags" -> optional(text)
				.verifying("Tag must be at least 3 characters", _.fold(true)(_.split(",").map(_.trim).find(_.length < 3).fold(true)(_ => false)))
				.verifying("You can't specify more than 5 tags", _.fold(true)(_.split(",").count(_.trim.nonEmpty) <= 5)),
			"content" -> nonEmptyText,
			"metaTitle" -> optional(text(maxLength = AppConstants.META_SIZE)),
			"metaDescr" -> optional(text(maxLength = AppConstants.META_SIZE)),
			"metaKeywords" -> optional(text(maxLength = AppConstants.META_SIZE)),
			"postType" -> nonEmptyText.verifying("wrong value", t => PostTypes.idByStr(t).nonEmpty))(PostData.apply)(PostData.unapply))

	// FIXME: should replaced with async json remove
	def removePost(id: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_OWN_REMOVE, Permission.PERM__POSTS_ANY_REMOVE), PatternType.REGEX)() { implicit request =>
		dap.posts.findPostById(id) flatMap {
			_.fold(asyncErrorRedirect("Post with id " + id + " not exists")) { post =>
				if (ac.actor.containsPermission(Permission.PERM__POSTS_ANY_REMOVE) ||
					(ac.actor.containsPermission(Permission.PERM__POSTS_OWN_REMOVE) && ac.actor.id == post.ownerId)) {
					dap.posts.removePostById(id) map { removed =>
						if (removed)
							successRedirect("Post with id " + id + " successfully removed!")
						else
							errorRedirect("Post with id " + id + " remove failed!")
					}
				} else future(Forbidden)
			}
		}
	}

	def checkPostCreateAccess[T](accessOk: Future[Result])(implicit request: Request[T], ac: AppContext): Future[Result] =
		if (ac.actor.containsPermission(Permission.PERM__POSTS_CREATE_ANYTIME))
			accessOk
		else if (ac.actor.containsPermission(Permission.PERM__POSTS_CREATE_CONDITIONAL))
			booleanOptionFold(Options.POSTS_CREATE_ALLOWED) {
				asyncErrorRedirect("Post creation not allowed now")
			} {
				accessOk
			}
		else
			future(Forbidden)

	def checkPostChangeAccess[T](id: Long)(accessOk: models.Post => Future[Result])(implicit request: Request[T], ac: AppContext): Future[Result] =
		dap.posts.findPostWithOwnerAndTagsById(id) flatMap {
			_.fold(asyncErrorRedirect("Post with id " + id + " not exists")) { post =>
				if (ac.actor.containsPermission(Permission.PERM__POSTS_ANY_EDIT_ANYTIME))
					accessOk(post)
				else if (ac.actor.containsPermission(Permission.PERM__POSTS_ANY_EDIT_CONDITIONAL)
					|| (ac.actor.containsPermission(Permission.PERM__POSTS_OWN_EDIT_CONDITIONAL) && ac.actor.id == post.ownerId))
					booleanOptionFold(Options.POSTS_CHANGE_ALLOWED) {
						asyncErrorRedirect("Post changing not allowed now")
					} {
						accessOk(post)
					}
				else
					future(Forbidden)
			}
		}

	def editPost(id: Long) = deadbolt.Pattern(Permission.OR(
		Permission.PERM__POSTS_ANY_EDIT_ANYTIME,
		Permission.PERM__POSTS_OWN_EDIT_ANYTIME,
		Permission.PERM__POSTS_ANY_EDIT_CONDITIONAL,
		Permission.PERM__POSTS_OWN_EDIT_CONDITIONAL), PatternType.REGEX)() { implicit request =>
		checkPostChangeAccess(id) { post =>
			future(Ok(views.html.admin.editPost(
				postForm.fill(PostData(
					post.title,
					if (post.tags.nonEmpty) Some(post.tags.map(_.name).mkString(",")) else None,
					post.content,
					post.metaTitle,
					post.metaDescr,
					post.metaKeywords,
					post.postType.toString)),
				id)))
		}
	}

	def updatePost(id: Long) = deadbolt.Pattern(Permission.OR(
		Permission.PERM__POSTS_ANY_EDIT_ANYTIME,
		Permission.PERM__POSTS_OWN_EDIT_ANYTIME,
		Permission.PERM__POSTS_ANY_EDIT_CONDITIONAL,
		Permission.PERM__POSTS_OWN_EDIT_CONDITIONAL), PatternType.REGEX)() { implicit request =>
		checkPostChangeAccess(id) { oldPost =>

			def redirectWithError(msg: String, form: Form[_]) =
				future(Ok(views.html.admin.editPost(form, id)(Flash(form.data) + ("error" -> msg), implicitly, implicitly, implicitly)))

			postForm.bindFromRequest.fold(
				formWithErrors => Future(BadRequest(views.html.admin.editPost(formWithErrors, id))), {
					postData =>
						dap.posts.updatePostWithTags(
							id,
							postData.title,
							postData.content,
							postData.metaTitle,
							postData.metaDescr,
							postData.metaKeywords,
							postData.getPostType,
							postData.getTags) flatMap { result =>
							if (result)
								future(Redirect(controllers.routes.PostsController.viewPost(id))
									.flashing("success" -> ("Post successfully updated!")))
							else
								redirectWithError("Some problems during post update!", postForm.fill(postData))
						}
				})

		}
	}

	def createPost = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_CREATE_CONDITIONAL, Permission.PERM__POSTS_CREATE_ANYTIME), PatternType.REGEX)() { implicit request =>
		checkPostCreateAccess {
			future(Ok(views.html.admin.createPost(postForm)))
		}
	}

	def viewPost(postId: Long) = deadbolt.WithAuthRequest()() { implicit request =>
		dap.posts.findPostWithOwnerAndTagsAndCommentsById(postId) map (_.fold(NotFound("Post not found")) { post =>
			if (post.postType == PostTypes.ARTICLE)
				Ok(views.html.app.viewPost(post))
			else
				NotFound("Article with id " + postId + " not found!")
		})
	}

	def viewPage(pageId: Long) = deadbolt.WithAuthRequest()() { implicit request =>
		dap.posts.findPostWithOwnerAndTagsById(pageId) map (_.fold(NotFound("Post not found")) { page =>
			if (page.postType == PostTypes.PAGE)
				Ok(views.html.app.viewPage(page))
			else
				NotFound("Page with id " + pageId + " not found!")
		})
	}

	def processCreatePost = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_CREATE_CONDITIONAL, Permission.PERM__POSTS_CREATE_ANYTIME), PatternType.REGEX)() { implicit request =>
		checkPostCreateAccess {
			postForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createPost(formWithErrors))), { postData =>
				dap.posts.createPostWithTags(
					ac.actor.id,
					postData.title,
					postData.content,
					postData.metaTitle,
					postData.metaDescr,
					postData.metaKeywords,
					postData.getPostType,
					postData.getTags) map { post =>
					Redirect(controllers.routes.PostsController.viewPost(post.id))
						.flashing("success" -> ("Post successfully created!"))
				}
			})
		}
	}

	def adminPostsListPage = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_ANY_LIST_VIEW, Permission.PERM__POSTS_OWN_LIST_VIEW), PatternType.REGEX)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.posts.postsWithAccountsAndTagsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt,
				None,
				None) map { items =>
				Ok(views.html.admin.parts.postsListPage(items))
			}
		}))
	}

	def adminPostsListPagesCount = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_ANY_LIST_VIEW, Permission.PERM__POSTS_OWN_LIST_VIEW), PatternType.REGEX)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.posts.postsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt,
				None,
				None) map { count => Ok(count.toString) }
		})
	}

	def adminPosts = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_ANY_LIST_VIEW, Permission.PERM__POSTS_OWN_LIST_VIEW), PatternType.REGEX)() { implicit request =>
		future(Ok(views.html.admin.posts()))
	}


	def postsByTagListPagesCount(tagName: String) = deadbolt.WithAuthRequest()(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			val preparedTag = tagName.trim.toLowerCase
			dap.posts.postsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt,
				None,
				if (preparedTag.isEmpty) None else Some(preparedTag)) map { count => Ok(count.toString) }
		})
	}

	def posts(pageId: Option[Int], tag: Option[String]) = deadbolt.WithAuthRequest()() { implicit request =>
		dap.posts.postsWithAccountsAndTagsListPage(
			AppConstants.DEFAULT_PAGE_SIZE,
			pageId.getOrElse(0),
			Seq.empty,
			None,
			None,
			tag) map { items => Ok(views.html.app.posts(items, pageId.getOrElse(1), tag)) }
	}


}

