package models

import controllers.AppConstants
import org.jsoup.Jsoup
import play.api.libs.json.{Json, Writes}

case class Comment(id: Long,
									 ownerId: Long,
									 targetType: CommentTargetTypes.CommentTargetTypes,
									 targetId: Long,
									 parentId: Option[Long],
									 contentType: CommentContentTypes.CommentTargetTypes,
									 content: String,
									 status: CommentStatusTypes.CommentStatus,
									 override val registered: Long,
									 owner: Option[Account],
									 target: Option[_],
									 parent: Option[Comment],
									 childs: Seq[Comment]) extends TraitModel with TraitDateSupports {

	def description: String =
		description(AppConstants.SHORT_DESCRIPTION_SIZE)

	def description(size: Int) = {
		val descr = Jsoup.parse(content).text()
		if (descr.size > size) descr.substring(0, size) + "..." else descr
	}

	def buildRootTree(comments: Seq[Comment]): Comment =
		copy(childs = comments.filter(_.parentId.fold(false)(_ == id)).sortBy(_.id).reverse.map { child =>
			child.copy(parent = Some(this)).buildRootTree(comments)
		})

}

object CommentStatusTypes extends Enumeration() {

	type CommentStatus = Value

	val NORMAL = Value("normal")

}

object CommentTargetTypes extends Enumeration() {

	type CommentTargetTypes = Value

	val POST = Value("post")

}

object CommentContentTypes extends Enumeration() {

	type CommentTargetTypes = Value

	val TEXT = Value("text")

	val HTML = Value("html")

	val MARKDOWN = Value("markdown")

}

object Comment {

	implicit class commentsTreeBuilder(comments: Seq[Comment]) {
		def buildTree: Seq[Comment] =
			comments
				.filter(c => c.parentId.fold(true)(!comments.map(_.id).contains(_)))
				.map(_ buildRootTree comments)
				.sortBy(_.id).reverse
	}

	implicit lazy val commentsTreeWrites: Writes[Comment] = new Writes[Comment] {

		import models.Account.accountsForCommentsWrites

		def writes(t: Comment) = Json.obj(
			"id" -> t.id,
			"ownerId" -> t.ownerId,
			"content" -> t.content,
			"registered" -> t.registered,
			"owner" -> t.owner,
			"childs" -> t.childs
		)
	}


	def apply(id: Long,
						ownerId: Long,
						targetType: CommentTargetTypes.CommentTargetTypes,
						targetId: Long,
						parentId: Option[Long],
						contentType: CommentContentTypes.CommentTargetTypes,
						content: String,
						status: CommentStatusTypes.CommentStatus,
						registered: Long,
						owner: Option[Account],
						target: Option[_],
						parent: Option[Comment],
						childs: Seq[Comment]): Comment =
		new Comment(
			id,
			ownerId,
			targetType,
			targetId,
			parentId,
			contentType,
			content,
			status,
			registered,
			owner,
			target,
			parent,
			childs)

	def apply(id: Long,
						ownerId: Long,
						targetType: CommentTargetTypes.CommentTargetTypes,
						targetId: Long,
						parentId: Option[Long],
						contentType: CommentContentTypes.CommentTargetTypes,
						content: String,
						status: CommentStatusTypes.CommentStatus,
						registered: Long): Comment =
		new Comment(
			id,
			ownerId,
			targetType,
			targetId,
			parentId,
			contentType,
			content,
			status,
			registered,
			None,
			None,
			None,
			Seq.empty)

}
