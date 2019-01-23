package models

import controllers.AppConstants
import org.jsoup.Jsoup

case class Post(
                 val id: Long,
                 val ownerId: Long,
                 val title: String,
                 val thumbnail: Option[String],
                 val content: String,
                 val status: PostStatus.PostStatus,
                 val created: Long,
                 val owner: Option[Account],
                 val tags: Seq[models.Tag],
								 val comments: Seq[Comment]) {

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

  def apply(
             id: Long,
             ownerId: Long,
             title: String,
             thumbnail: Option[String],
             content: String,
             status: PostStatus.PostStatus,
             created: Long,
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
      owner,
      tags,
			comments)

  def apply(
             id: Long,
             ownerId: Long,
             title: String,
             thumbnail: Option[String],
             content: String,
             status: PostStatus.PostStatus,
             created: Long): Post =
    new Post(
      id: Long,
      ownerId,
      title,
      thumbnail,
      content,
      status,
      created,
      None,
      Seq.empty,
			Seq.empty)

}
