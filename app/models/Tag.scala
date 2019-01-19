package models

case class Tag(
                val id: Long,
                val name: String,
                val descr: Option[String],
                val assignPermissions: Seq[Permission])
  extends be.objectify.deadbolt.scala.models.Role {

  override def equals(obj: Any): Boolean =
    obj.isInstanceOf[Tag] && obj.asInstanceOf[Tag] == id

  override def toString: String = "Tag(" + id + "\"" + name + "\")"

}

object Tag {

  def apply(
             id: Long,
             name: String,
             descr: Option[String]): Tag =
    new Tag(
      id,
      name,
      descr,
      Seq.empty)

  def apply(
             id: Long,
             name: String,
             descr: Option[String],
             assignPermissions: Seq[Permission]): Tag =
    new Tag(
      id,
      name,
      descr,
      assignPermissions)


}

object TagTargetTypes extends Enumeration() {

  type TagTargetTypes = Value

  val POST = Value("post")

}

