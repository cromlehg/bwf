package models.dao

import javax.inject.Inject
import models.{Comment, CommentContentTypes, CommentStatusTypes, CommentTargetTypes}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait CommentDAO {

	def findById(id: Long): Future[Option[Comment]]

	def createComment(ownerId: Long,
										targetType: CommentTargetTypes.CommentTargetTypes,
										targetId: Long,
										parentId: Option[Long],
										contentType: CommentContentTypes.CommentTargetTypes,
										content: String,
										status: CommentStatusTypes.CommentStatus): Future[Comment]

	def existsCommentById(id: Long): Future[Boolean]

	def updateComment(commentId: Long, content: String): Future[Boolean]

	def allCommentsForTargetWithAccounts(targetId: Long, targetType: CommentTargetTypes.CommentTargetTypes)(accountDAO: AccountDAO): Future[Seq[Comment]]

	def commentsListPage(pSize: Int,
											 pId: Int,
											 sortsBy: Seq[(String, Boolean)],
											 filterOpt: Option[String],
											 target: Option[(Long, CommentTargetTypes.CommentTargetTypes)],
											 ownerId: Option[Long])(postDAO: PostDAO): Future[Seq[Comment]]

	def commentsListPagesCount(pSize: Int,
														 filterOpt: Option[String],
														 target: Option[(Long, CommentTargetTypes.CommentTargetTypes)],
														 ownerId: Option[Long]): Future[Int]

	def close: Future[Unit]

}

class CommentDAOCloseHook @Inject()(dao: CommentDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
