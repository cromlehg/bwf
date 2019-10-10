package models

import controllers.AppConstants
import org.jsoup.Jsoup
import play.i18n.Messages

case class Post(id: Long,
								ownerId: Long,
								title: String,
								thumbnail: Option[String],
								content: String,
								status: PostStatus.PostStatus,
								created: Long,
								metaTitle: Option[String],
								metaDescr: Option[String],
								metaKeywords: Option[String],
								postType: PostTypes.PostType,
								owner: Option[Account],
								tags: Seq[models.Tag],
								comments: Seq[Comment]) {

	def description: String =
		description(AppConstants.DESCRIPTION_SIZE)

	def description(size: Int) = {
		val descr = Jsoup.parse(content).text()
		if (descr.size > size) descr.substring(0, size) + "..." else descr
	}

	def getTrimedTitle(size: Int) =
		if (title.size > size) title.substring(0, size) + "..." else title

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(created))

	val thumbnailOpt: Option[String] =
		(thumbnail orElse Post.firstImage(content)).orElse(Some("/assets/images/empty.png"))

	def postTypeHRId: String =
		PostTypes.postTypeToMsgID
			.get(postType)
			.getOrElse("post.type.unknown")

}

object PostTypes extends Enumeration() {

	type PostType = Value

	val PAGE = Value("page")

	val ARTICLE = Value("article")

	def idByStr(str: String): Option[PostType] =
		str match {
			case "page" => Some(PAGE)
			case "article" => Some(ARTICLE)
			case _ => None
		}

	def postTypeToMsgID: Map[Value, String] =
		Map(PostTypes.PAGE -> "post.type.page",
			PostTypes.ARTICLE -> "post.type.article")

	def strToMsgID: Map[String, String] =
		postTypeToMsgID
			.map(t => (t._1.toString, t._2))

}

object PostStatus extends Enumeration() {

	type PostStatus = Value

	val DRAFT = Value("draft")

	val SANDBOX = Value("sandbox")

	val PUBLISHED = Value("published")

}

object Post {

	val pattern = """<\s*(i|I)(m|M)(g|G).*?(s|S)(r|R)(c|C)\s*=\s*\"(.*?)".*?>""".r

	def firstImage(content: String): Option[String] =
		pattern.findFirstMatchIn(content).map(_ group 7)

	def apply(id: Long,
						ownerId: Long,
						title: String,
						thumbnail: Option[String],
						content: String,
						status: PostStatus.PostStatus,
						created: Long,
						metaTitle: Option[String],
						metaDescr: Option[String],
						metaKeywords: Option[String],
						postType: PostTypes.PostType,
						owner: Option[Account],
						tags: Seq[models.Tag],
						comments: Seq[models.Comment]): Post =
		new Post(
			id: Long,
			ownerId,
			title,
			thumbnail,
			content,
			status,
			created,
			metaTitle,
			metaDescr,
			metaKeywords,
			postType,
			owner,
			tags,
			comments)

	def apply(id: Long,
						ownerId: Long,
						title: String,
						thumbnail: Option[String],
						content: String,
						status: PostStatus.PostStatus,
						created: Long,
						metaTitle: Option[String],
						metaDescr: Option[String],
						metaKeywords: Option[String],
						postType: PostTypes.PostType): Post =
		new Post(
			id: Long,
			ownerId,
			title,
			thumbnail,
			content,
			status,
			created,
			metaTitle,
			metaDescr,
			metaKeywords,
			postType,
			None,
			Seq.empty,
			Seq.empty)

}
