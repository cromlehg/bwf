package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.models.PatternType
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{Options, Permission}
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.libs.json._
import play.api.mvc.{ControllerComponents, Flash, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostsController @Inject()(
                                 cc: ControllerComponents,
                                 deadbolt: DeadboltActions,
                                 accountDAO: AccountDAO,
                                 sessionDAO: SessionDAO,
                                 postDAO: PostDAO,
                                 config: Configuration)(implicit ec: ExecutionContext, optionDAO: OptionDAO, menuDAO: MenuDAO)
  extends CommonAbstractController(optionDAO, cc) with JSONSupport {

  import scala.concurrent.Future.{successful => future}

  case class PostData(
                       val title: String,
                       val tags: Option[String],
                       val content: String) {

    val getTags: Seq[String] =
      tags.fold(Seq.empty[String])(_.split(",").map(_.trim.toLowerCase).filter(_.nonEmpty))

  }

  val postForm = Form(
    mapping(
      "title" -> nonEmptyText(3, 100),
      "tags" -> optional(text)
        .verifying("Tag must be at least 3 characters", _.fold(true)(_.split(",").map(_.trim).find(_.length < 3).fold(true)(_ => false)))
        .verifying("You can't specify more than 5 tags", _.fold(true)(_.split(",").count(_.trim.nonEmpty) <= 5)),
      "content" -> nonEmptyText)(PostData.apply)(PostData.unapply))

  // FIXME: should replaced with async json remove
  def removePost(id: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_OWN_REMOVE, Permission.PERM__POSTS_ANY_REMOVE), PatternType.REGEX)() { implicit request =>
    postDAO.findPostById(id) flatMap {
      _.fold(asyncErrorRedirect("Post with id " + id + " not exists")) { post =>
        if (ac.actor.containsPermission(Permission.PERM__POSTS_ANY_REMOVE) ||
          (ac.actor.containsPermission(Permission.PERM__POSTS_OWN_REMOVE) && ac.actor.id == post.ownerId)) {
          postDAO.removePostById(id) map { removed =>
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
    postDAO.findPostWithOwnerAndTagsById(id) flatMap {
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
          post.content)),
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
            postDAO.updatePostWithTags(
              id,
              postData.title,
              postData.content,
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
    postDAO.findPostWithOwnerAndTagsById(postId) map (_.fold(NotFound("Post not found")) { post =>
      Ok(views.html.app.viewPost(post))
    })
  }

  def viewPage(postId: Long) = deadbolt.WithAuthRequest()() { implicit request =>
    postDAO.findPostWithOwnerAndTagsById(postId) map (_.fold(NotFound("Post not found")) { page =>
      Ok(views.html.app.viewPage(page))
    })
  }

  def processCreatePost = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_CREATE_CONDITIONAL, Permission.PERM__POSTS_CREATE_ANYTIME), PatternType.REGEX)() { implicit request =>
    checkPostCreateAccess {
      postForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.admin.createPost(formWithErrors))), { postData =>
        postDAO.createPostWithTags(
          ac.actor.id,
          postData.title,
          postData.content,
          postData.getTags) map { post =>
          Redirect(controllers.routes.PostsController.viewPost(post.id))
            .flashing("success" -> ("Post successfully created!"))
        }
      })
    }
  }

  def adminPostsListPage = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_ANY_LIST_VIEW, Permission.PERM__POSTS_OWN_LIST_VIEW), PatternType.REGEX)(parse.json) { implicit request =>
    fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
      postDAO.postsWithAccountsListPage(
        pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
        pageIdOpt.getOrElse(0),
        Seq.empty,
        filterOpt,
        None) map { items =>
        Ok(views.html.admin.parts.postsListPage(items))
      }
    }))
  }

  def adminPostsListPagesCount = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_ANY_LIST_VIEW, Permission.PERM__POSTS_OWN_LIST_VIEW), PatternType.REGEX)(parse.json) { implicit request =>
    fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
      postDAO.postsListPagesCount(
        pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
        filterOpt,
        None) map { count => Ok(count.toString) }
    })
  }

  def adminPosts = deadbolt.Pattern(Permission.OR(Permission.PERM__POSTS_ANY_LIST_VIEW, Permission.PERM__POSTS_OWN_LIST_VIEW), PatternType.REGEX)() { implicit request =>
    future(Ok(views.html.admin.posts()))
  }

//	def postsByTagListPage(tagName: String) = deadbolt.WithAuthRequest()() { implicit request =>
//		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
//			postDAO.postsByTagWithAccountsListPage(
//				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
//				pageIdOpt.getOrElse(0),
//				Seq.empty,
//				filterOpt,
//				None) map { items =>
//				Ok(views.html.admin.parts.postsListPage(items))
//			}
//		}))
//	}
//
//	def postsByTagListPagesCount(tagName: String) = deadbolt.WithAuthRequest()() { implicit request =>
//		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
//			postDAO.postsByTagListPagesCount(
//				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
//				filterOpt,
//				None,
//				tagName) map { count => Ok(count.toString) }
//		})
//	}
//
//	def postsByTag(tagName: String) = deadbolt.WithAuthRequest()() { implicit request =>
//		future(Ok(views.html.admin.posts()))
//	}

  private def withNameOrIdOpt(
                               idFieldName: String,
                               nameFieldName: String,
                               idByName: String => Future[Option[Long]])(
                               f1: Long => Future[Result])(
                               f2: Future[Result])(implicit request: Request[JsValue], ac: AppContext): Future[Result] =
    fieldLongOpt(idFieldName)(_.fold(fieldStringOpt(nameFieldName)(_.fold(f2) { str =>
      val prepared = str.trim()
      if (prepared.length < 1) future(BadRequest("Should be 1 symbol at least")) else
        idByName(prepared) flatMap (_.fold(future(BadRequest("Not found")))(f1))
    }))(f1))

  private def withNameOrIdSingleOpt(
                                     idFieldName: String,
                                     nameFieldName: String,
                                     idByName: String => Future[Option[Long]])(
                                     f: Option[Long] => Future[Result])(implicit request: Request[JsValue], ac: AppContext): Future[Result] =
    withNameOrIdOpt(idFieldName, nameFieldName, idByName)(t => f(Some(t)))(f(None))

  private def withAccountNameOrIdSingleOpt(f: Option[Long] => Future[Result])(implicit request: Request[JsValue], ac: AppContext): Future[Result] =
    withNameOrIdSingleOpt("account_id", "login", t => accountDAO.findAccountOptByLoginOrEmail(t).map(_.map(_.id)))(f)


}

