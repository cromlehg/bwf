package models

import controllers.AppConstants
import org.jsoup.Jsoup

case class Comment(
										val id: Long,
										val ownerId: Long,
										val targetType: CommentTargetTypes.CommentTargetTypes,
										val targetId: Long,
										val parentId: Option[Long],
										val contentType: CommentContentTypes.CommentTargetTypes,
										val content: String,
										val status: CommentStatusTypes.CommentStatus,
										val created: Long,
										val owner: Option[Account],
										val target: Option[_],
										val parent: Option[Comment],
										val childs: Seq[Comment]) {

	def description: String =
		description(AppConstants.SHORT_DESCRIPTION_SIZE)

	def description(size: Int) = {
		val descr = Jsoup.parse(content).text()
		if (descr.size > size) descr.substring(0, size) + "..." else descr
	}

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(created))

	def buildRootTree(comments: Seq[Comment]): Comment =
		copy(childs = comments.filter(_.parentId.fold(false)(_ == id)).sortBy(_.id).reverse.map { child =>
			child.copy(parent = Some(this)).buildRootTree(comments)
		})

}


object Comments {

	implicit class CommentsTreeBuilder(comments: Seq[Comment]) {

		def buildTree: Seq[Comment] =
			comments
				.filter(c => c.parentId.fold(true)(!comments.map(_.id).contains(_)))
				.map(_ buildRootTree comments)
				.sortBy(_.id).reverse

	}

}

object CommentStatusTypes extends Enumeration() {

	type CommentStatus = Value

	val POST = Value("normal")

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

	def apply(id: Long,
						ownerId: Long,
						targetType: CommentTargetTypes.CommentTargetTypes,
						targetId: Long,
						parentId: Option[Long],
						contentType: CommentContentTypes.CommentTargetTypes,
						content: String,
						status: CommentStatusTypes.CommentStatus,
						created: Long,
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
			created,
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
						created: Long): Comment =
		new Comment(
			id,
			ownerId,
			targetType,
			targetId,
			parentId,
			contentType,
			content,
			status,
			created,
			None,
			None,
			None,
			Seq.empty)

}
