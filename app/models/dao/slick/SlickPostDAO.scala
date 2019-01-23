package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.dao.PostDAO
import models.dao.slick.table.PostTable
import models.{CommentTargetTypes, Post, PostStatus, TagTargetTypes}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickPostDAO @Inject()(
															val accountDAO: SlickAccountDAO,
															val commentDAO: SlickCommentDAO,
															val tagDAO: SlickTagDAO,
															val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends PostDAO with PostTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	private val queryExistsById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id).exists)

	def _findPostOptById(id: Long) =
		queryById(id).result.headOption

	def _removePostById(id: Long) =
		queryById(id).delete.map(_ == 1)

	def _findPostWithOwnerById(id: Long) =
		for {
			postOpt <- _findPostOptById(id)
			ownerOpt <- maybeOptAction(postOpt)(t => accountDAO._findAccountOptById(t.ownerId))
		} yield postOpt.map(_.copy(owner = ownerOpt))

	def _findPostWithOwnerAndTagsById(id: Long) =
		for {
			postOpt <- _findPostWithOwnerById(id)
			tags <- maybeOptActionSeqR(postOpt)(t => tagDAO._findTagsByTargetId(t.id, TagTargetTypes.POST).result)
		} yield postOpt.map(_.copy(tags = tags))

	def _findPostWithOwnerAndTagsAndCommentsById(id: Long) = {
		import models.Comment.commentsTreeBuilder
		for {
			postOpt <- _findPostWithOwnerAndTagsById(id)
			comments <- maybeOptActionSeqR(postOpt)(t => commentDAO._allCommentsForTargetWithAccounts(t.id, CommentTargetTypes.POST)(accountDAO))
		} yield postOpt.map(_.copy(comments = comments.buildTree))
	}

	def _updatePost(id: Long, title: String, content: String) =
		table
			.filter(_.id === id)
			.map(t => (t.title, t.content))
			.update(title, content)
			.map(_ == 1)

	def _postsWithAccountsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
		_postsListPage(pSize, pId, sortsBy, filterOpt, ownerId)
			.join(accountDAO.table).on(_.ownerId === _.id)

	def _postsWithAccountsListPageAction(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
		_postsListPage(pSize, pId, sortsBy, filterOpt, ownerId)
			.join(accountDAO.table).on(_.ownerId === _.id)
			.result
			.map(_.map { case (post, account) => post.copy(owner = Some(account)) })

	def _postsWithAccountsWithTagFilterListPageAction(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long], tag: Option[String]) =
		_postsByTagListPage(pSize, pId, sortsBy, filterOpt, ownerId, tag)
			.join(accountDAO.table).on(_.ownerId === _.id)
			.result
			.map(_.map { case (post, account) => post.copy(owner = Some(account)) })

	implicit val TagTargetTypesMapper = enum2String(TagTargetTypes)

	def _postsByTagListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long], tag: Option[String]) =
		table
			.filterOpt(ownerId) { case (t, filter) => t.ownerId === filter }
			.filterOpt(filterOpt) { case (t, filter) => t.title.like("%" + filter.trim + "%") || t.content.like("%" + filter.trim + "%") }
  		.filterOpt(tag) {	case (t,tagName) =>
					t.id in tagDAO.tableTagToTarget
						.filter(_.targetType === TagTargetTypes.POST)
						.filter(_.tagId in tagDAO.tableTag
								.filter(_.name.trim.toLowerCase === tagName.trim.toLowerCase)
								.map(_.id))
						.map(_.targetId)
			}
			.dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
			.page(pSize, pId)


	def _postsWithTagFilterListPagesCount(pSize: Int, filterOpt: Option[String], ownerId: Option[Long], tag: Option[String]) =
		table
			.filterOpt(ownerId) { case (t, filter) => t.ownerId === filter }
			.filterOpt(filterOpt) { case (t, filter) => t.title.like("%" + filter.trim + "%") || t.content.like("%" + filter.trim + "%") }
			.filterOpt(tag) {	case (t,tagName) =>
				t.id in tagDAO.tableTagToTarget
					.filter(_.targetType === TagTargetTypes.POST)
					.filter(_.tagId in tagDAO.tableTag
						.filter(_.name.trim.toLowerCase === tagName.trim.toLowerCase)
						.map(_.id))
					.map(_.targetId)
			}
			.size

	def _postsWithAccountsAndTagsWithTagFilterListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long], tag: Option[String]) =
		for {
			posts: Seq[Post] <- _postsWithAccountsWithTagFilterListPageAction(pSize, pId, sortsBy, filterOpt, ownerId, tag)
			tagsWithAssigns <- tagDAO._findTagsByTargetIdsWithAssigns(posts.map(_.id), TagTargetTypes.POST).result
		} yield posts.map { post: Post =>
			post.copy(tags = tagsWithAssigns.filter(_._1._3 == post.id).map(_._2))
		}

	def _postsWithAccountsAndTagsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
		for {
			posts: Seq[Post] <- _postsWithAccountsListPageAction(pSize, pId, sortsBy, filterOpt, ownerId)
			tagsWithAssigns <- tagDAO._findTagsByTargetIdsWithAssigns(posts.map(_.id), TagTargetTypes.POST).result
		} yield posts.map { post: Post =>
			post.copy(tags = tagsWithAssigns.filter(_._1._3 == post.id).map(_._2))
		}

	def _postsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
		table
			.filterOpt(ownerId) { case (t, filter) => t.ownerId === filter }
			.filterOpt(filterOpt) { case (t, filter) => t.title.like("%" + filter.trim + "%") || t.content.like("%" + filter.trim + "%") }
			.dynamicSortBy(if (sortsBy.isEmpty) Seq(("id", Desc)) else sortsBy)
			.page(pSize, pId)

	def _postsListPagesCount(pSize: Int, filterOpt: Option[String], ownerId: Option[Long]) =
		table
			.filterOpt(ownerId) { case (t, filter) => t.ownerId === filter }
			.filterOpt(filterOpt) { case (t, filter) => t.title.like("%" + filter.trim + "%") || t.content.like("%" + filter.trim + "%") }
			.size

	def _createPost(ownerId: Long, title: String, content: String) =
		table returning table.map(_.id) into ((v, id) => v.copy(id = id)) += models.Post(
			0,
			ownerId,
			title,
			None,
			content,
			PostStatus.DRAFT,
			System.currentTimeMillis)


	def _updatePostWithTags(id: Long, title: String, content: String, tags: Seq[String]) =
		_updatePost(id, title, content) flatMap { success =>
			if (success)
				tagDAO._refreshTags(tags, id, TagTargetTypes.POST).map(_ => true)
			else
				DBIO.successful(false)
		}

	def _createPostWithTags(ownerId: Long, title: String, content: String, tags: Seq[String]) =
		_createPost(ownerId, title, content) flatMap { post =>
			tagDAO._createTagsIfNotExistsAndAssignToTargetIfNotAssigned(tags, post.id, TagTargetTypes.POST).map { tags =>
				post.copy(tags = tags)
			}
		}

	def _existsPostById(id: Long) =
		queryExistsById(id)

	override def existsPostById(id: Long): Future[Boolean] =
		db.run(_existsPostById(id).result)

	override def updatePostWithTags(id: Long, title: String, content: String, tags: Seq[String]): Future[Boolean] =
		db.run(_updatePostWithTags(id, title, content, tags).transactionally)

	override def createPostWithTags(ownerId: Long, title: String, content: String, tags: Seq[String]): Future[Post] =
		db.run(_createPostWithTags(ownerId, title, content, tags).transactionally)

	override def updatePost(id: Long, title: String, content: String): Future[Boolean] =
		db.run(_updatePost(id, title, content).transactionally)

	override def removePostById(id: Long): Future[Boolean] =
		db.run(_removePostById(id).transactionally)

	override def findPostById(id: Long): Future[Option[Post]] =
		db.run(_findPostOptById(id: Long))

	override def findPostWithOwnerById(id: Long): Future[Option[Post]] =
		db.run(_findPostWithOwnerById(id: Long))

	override def findPostWithOwnerAndTagsById(id: Long): Future[Option[Post]] =
		db.run(_findPostWithOwnerAndTagsById(id: Long))

	override def findPostWithOwnerAndTagsAndCommentsById(id: Long): Future[Option[Post]] =
		db.run(_findPostWithOwnerAndTagsAndCommentsById(id))

	override def createPost(ownerId: Long, title: String, content: String): Future[Post] =
		db.run(_createPost(ownerId, title, content).transactionally)

	override def postsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String], ownerId: Option[Long]): Future[Seq[Post]] =
		db.run(_postsListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt, ownerId).result)

	override def postsListPagesCount(pSize: Int, filterOpt: Option[String], ownerId: Option[Long], tag: Option[String]): Future[Int] =
		db.run(_postsWithTagFilterListPagesCount(pSize, filterOpt, ownerId, tag).result).map(t => pages(t, pSize))

	override def postsWithAccountsAndTagsListPage(pSize: Int,
																								pId: Int,
																								sortsBy: Seq[(String, Boolean)],
																								filterOpt: Option[String],
																								ownerId: Option[Long],
																								tag: Option[String]): Future[Seq[Post]] =
		db.run(_postsWithAccountsAndTagsWithTagFilterListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt, ownerId, tag))

	override def close: Future[Unit] =
		future(db.close())

}
