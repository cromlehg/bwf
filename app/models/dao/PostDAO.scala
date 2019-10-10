package models.dao

import javax.inject.Inject
import models.{Post, PostTypes}
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

trait PostDAO {

	def updatePost(id: Long,
								 title: String,
								 content: String,
								 metaTitle: Option[String],
								 metaDescr: Option[String],
								 metaKeywords: Option[String],
								 postType: PostTypes.PostType): Future[Boolean]

	def updatePostWithTags(id: Long,
												 title: String,
												 content: String,
												 metaTitle: Option[String],
												 metaDescr: Option[String],
												 metaKeywords: Option[String],
												 postType: PostTypes.PostType,
												 tags: Seq[String]): Future[Boolean]

	def findPostById(id: Long): Future[Option[Post]]

	def existsPostById(id: Long): Future[Boolean]

	def removePostById(id: Long): Future[Boolean]

	def findPostWithOwnerById(id: Long): Future[Option[Post]]

	def findPostWithOwnerAndTagsById(id: Long): Future[Option[Post]]

	def findPostWithOwnerAndTagsAndCommentsById(id: Long): Future[Option[Post]]

	def createPost(ownerId: Long,
								 title: String,
								 content: String,
								 metaTitle: Option[String],
								 metaDescr: Option[String],
								 metaKeywords: Option[String],
								 postType: PostTypes.PostType): Future[Post]

	def createPostWithTags(ownerId: Long,
												 title: String,
												 content: String,
												 metaTitle: Option[String],
												 metaDescr: Option[String],
												 metaKeywords: Option[String],
												 postType: PostTypes.PostType,
												 tags: Seq[String]): Future[Post]

	def postsListPage(pSize: Int,
										pId: Int,
										sortsBy: Seq[(String, Boolean)],
										filterOpt: Option[String],
										ownerId: Option[Long]): Future[Seq[Post]]

	def postsListPagesCount(pSize: Int,
													filterOpt: Option[String],
													ownerId: Option[Long],
													tag: Option[String]): Future[Int]

	def postsWithAccountsAndTagsListPage(pSize: Int,
																			 pId: Int,
																			 sortsBy: Seq[(String, Boolean)],
																			 filterOpt: Option[String],
																			 ownerId: Option[Long],
																			 tag: Option[String]): Future[Seq[Post]]

	def close: Future[Unit]

}

class PostDAOCloseHook @Inject()(dao: PostDAO, lifecycle: ApplicationLifecycle) {
	lifecycle.addStopHook { () =>
		Future.successful(dao.close)
	}
}
