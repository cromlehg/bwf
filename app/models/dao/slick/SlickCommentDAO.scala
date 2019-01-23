package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.CommentTargetTypes.CommentTargetTypes
import models.dao.slick.table.CommentTable
import models.dao.{AccountDAO, CommentDAO, PostDAO}
import models.{Comment, CommentContentTypes, CommentStatusTypes, CommentTargetTypes}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{existentials, higherKinds}

@Singleton
class SlickCommentDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider,
																val accountDAO: SlickAccountDAO)(implicit ec: ExecutionContext)
	extends CommentDAO with CommentTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	private val queryExistsById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id).exists)

	def _findById(id: Long) =
		queryById(id).result.headOption

	def _commentsListPage(pSize: Int,
												pId: Int,
												sortsBy: Seq[(String, Direction)],
												filterOpt: Option[String],
												target: Option[(Long, CommentTargetTypes)],
												ownerId: Option[Long])(postDAO: PostDAO) =
		table
			.filterOpt(target) { case (t, (targetId, targetType)) => t.targetId === targetId && t.targetType === targetType }
			.filterOpt(ownerId)(_.ownerId === _)
			.filterOpt(filterOpt) { case (t, filter) => t.content.like("%" + filter.trim + "%") }
			.dynamicSortBy(sortsBy)
			.page(pSize, pId)
			.join(accountDAO.table).on(_.ownerId === _.id)
			.joinLeft(postDAO.asInstanceOf[SlickPostDAO].table).on { case ((comment, account), post) =>
			comment.targetType === CommentTargetTypes.POST && comment.targetId === post.id
		}

	def _commentsListPagesCount(pSize: Int,
															filterOpt: Option[String],
															target: Option[(Long, CommentTargetTypes)],
															ownerId: Option[Long]) =
		table
			.filterOpt(target) { case (t, (targetId, targetType)) => t.targetId === targetId && t.targetType === targetType }
			.filterOpt(ownerId)(_.ownerId === _)
			.filterOpt(filterOpt) { case (t, filter) => t.content.like("%" + filter.trim + "%") }
			.size

	def _createComment(ownerId: Long,
										 targetType: CommentTargetTypes.CommentTargetTypes,
										 targetId: Long,
										 parentId: Option[Long],
										 contentType: CommentContentTypes.CommentTargetTypes,
										 content: String,
										 status: CommentStatusTypes.CommentStatus) =
		table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += Comment(
			0,
			ownerId,
			targetType,
			targetId,
			parentId,
			contentType,
			content,
			status,
			System.currentTimeMillis)

	override def findById(id: Long): Future[Option[Comment]] =
		db.run(_findById(id))

	override def commentsListPage(pSize: Int,
																pId: Int,
																sortsBy: Seq[(String, Boolean)],
																filterOpt: Option[String],
																target: Option[(Long, CommentTargetTypes)],
																ownerId: Option[Long])(postDAO: PostDAO): Future[Seq[Comment]] =
		db.run(_commentsListPage(
			pSize,
			pId,
			sortsBy.map(t => (t._1, if (t._2) Asc else Desc)),
			filterOpt,
			target,
			ownerId)(postDAO)
			.result
			.map(_.map { case ((comment, owner), post) => comment.copy(owner = Some(owner), target = post) }))

	def _updateComment(commentId: Long, content: String) =
		table
			.filter(_.id === commentId)
			.map(_.content)
			.update(content)
			.map(_ == 1)

	def _allCommentsForTargetWithAccounts(targetId: Long, targetType: CommentTargetTypes.CommentTargetTypes)(accountDAO: AccountDAO) =
		for {
			comments <- table
				.filter(_.targetType === targetType)
				.filter(_.targetId === targetId).result
			accounts <- accountDAO.asInstanceOf[SlickAccountDAO]._findAccounts(comments.map(_.ownerId).distinct)
		} yield comments.map(c => c.copy(owner = accounts.find(_.id == c.ownerId)))

	def _existsCommentById(id: Long) =
		queryExistsById(id)

	override def existsCommentById(id: Long): Future[Boolean] =
		db.run(_existsCommentById(id).result)

	override def commentsListPagesCount(pSize: Int,
																			filterOpt: Option[String],
																			target: Option[(Long, CommentTargetTypes)],
																			ownerId: Option[Long]): Future[Int] =
		db.run(_commentsListPagesCount(
			pSize,
			filterOpt,
			target,
			ownerId).result).map(t => pages(t, pSize))

	override def updateComment(commentId: Long, content: String): Future[Boolean] =
		db.run(_updateComment(commentId, content).transactionally)

	override def createComment(ownerId: Long,
														 targetType: CommentTargetTypes.CommentTargetTypes,
														 targetId: Long,
														 parentId: Option[Long],
														 contentType: CommentContentTypes.CommentTargetTypes,
														 content: String,
														 status: CommentStatusTypes.CommentStatus): Future[Comment] =
		db.run(_createComment(
			ownerId,
			targetType,
			targetId,
			parentId,
			contentType,
			content,
			status).transactionally)

	def allCommentsForTargetWithAccounts(targetId: Long, targetType: CommentTargetTypes.CommentTargetTypes)(accountDAO: AccountDAO): Future[Seq[Comment]] =
		db.run(_allCommentsForTargetWithAccounts(targetId, targetType)(accountDAO))

	override def close: Future[Unit] =
		future(db.close())

}
