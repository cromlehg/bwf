package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.dao.PostDAO
import models.dao.slick.table.PostTable
import models.{Post, PostStatus, TagTargetTypes}
import play.api.db.slick.DatabaseConfigProvider
import slick.ast.Ordering.{Asc, Desc, Direction}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickPostDAO @Inject()(
															val accountDAO: SlickAccountDAO,
															val tagDAO: SlickTagDAO,
															val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends PostDAO with PostTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	def _findPostOptById(id: Long) =
		queryById(id).result.headOption

	def _removePostById(id: Long) =
		queryById(id).delete.map(_ == 1)

	def _findPostWithOwnerById(id: Long) = {
		val query = for {
			postOpt <- _findPostOptById(id)
			ownerOpt <- maybeOptAction(postOpt)(t => accountDAO._findAccountOptById(t.ownerId))
		} yield (postOpt, ownerOpt)

		query map {
			case (postOpt, accountOpt) => postOpt.map(_.copy(owner = accountOpt))
		}
	}

	def _findPostWithOwnerAndTagsById(id: Long) = {
		val query = for {
			postOpt <- _findPostOptById(id)
			tags <- maybeOptActionSeqR(postOpt)(t => tagDAO._findTagsByTargetId(t.id, TagTargetTypes.POST).result)
		} yield (postOpt, tags)

		query map {
			case (postOpt, tags) => postOpt.map(_.copy(tags = tags))
		}
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

	def _postsWithAccountsAndTagsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
		for {
			posts: Seq[Post] <- _postsWithAccountsListPageAction(pSize, pId, sortsBy, filterOpt, ownerId)
			tagsWithAssigns <- tagDAO._findTagsByTargetIdsWithAssigns(posts.map(_.id), TagTargetTypes.POST).result
		} yield posts.map { post: Post =>
			post.copy(tags = tagsWithAssigns.filter(_._1._3 == post.id).map(_._2))
		}

	def _postsWithAccountsAndCategoriesListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Direction)], filterOpt: Option[String], ownerId: Option[Long]) =
		_postsWithAccountsListPage(pSize, pId, sortsBy, filterOpt, ownerId)

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

	override def createPost(ownerId: Long, title: String, content: String): Future[Post] =
		db.run(_createPost(ownerId, title, content).transactionally)

	override def postsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String], ownerId: Option[Long]): Future[Seq[Post]] =
		db.run(_postsListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt, ownerId).result)

	override def postsListPagesCount(pSize: Int, filterOpt: Option[String], ownerId: Option[Long]): Future[Int] =
		db.run(_postsListPagesCount(pSize, filterOpt, ownerId).result).map(t => pages(t, pSize))

	override def postsWithAccountsListPage(pSize: Int, pId: Int, sortsBy: Seq[(String, Boolean)], filterOpt: Option[String], ownerId: Option[Long]): Future[Seq[Post]] =
		db.run(_postsWithAccountsAndTagsListPage(pSize, pId, sortsBy.map(t => (t._1, if (t._2) Asc else Desc)), filterOpt, ownerId))

	override def close: Future[Unit] =
		future(db.close())

}
