package models

case class Role(
                 val id: Long,
                 override val name: String,
                 descr: Option[String],
                 permissions: Seq[Permission])
  extends be.objectify.deadbolt.scala.models.Role {

  override def equals(obj: Any): Boolean =
    obj.isInstanceOf[Role] && obj.equals(name)

  override def toString: String = name

}

case class Permission(
                       val id: Long,
                       override val value: String,
                       descr: Option[String])
  extends be.objectify.deadbolt.scala.models.Permission {

  override def equals(obj: Any): Boolean =
    obj.isInstanceOf[Permission] && obj.equals(value)

  override def toString: String = value

}

object Role {

  val ROLE_ADMIN = "admin"

  val ROLE_EDITOR = "editor"

  val ROLE_WRITER = "writer"

  val ROLE_CLIENT = "client"

  def apply(
             id: Long,
             name: String,
             descr: Option[String]): Role =
    new Role(
      id,
      name,
      descr,
      Seq.empty)

  def apply(
             id: Long,
             name: String,
             descr: Option[String],
             permissions: Seq[Permission]): Role =
    new Role(
      id,
      name,
      descr,
      permissions)


}

object Permission {

  val PERM__POSTS_OWN_EDIT_ANYTIME = "posts.own.edit.anytime"
  val PERM__POSTS_ANY_EDIT_ANYTIME = "posts.any.edit.anytime"
  val PERM__POSTS_OWN_EDIT_CONDITIONAL = "posts.own.edit.conditional"
  val PERM__POSTS_ANY_EDIT_CONDITIONAL = "posts.any.edit.conditional"
  val PERM__POSTS_CREATE_CONDITIONAL = "posts.create.conditional"
  val PERM__POSTS_CREATE_ANYTIME = "posts.create.anytime"
  val PERM__POSTS_OWN_REMOVE = "posts.own.remove"
  val PERM__POSTS_ANY_REMOVE = "posts.any.remove"
  val PERM__POSTS_OPEN_VIEW = "posts.open.view"
  val PERM__POSTS_ANY_VIEW = "posts.any.view"
  val PERM__POSTS_OWN_LIST_VIEW = "posts.own.list.view"
  val PERM__POSTS_ANY_LIST_VIEW = "posts.any.list.view"

	val PERM__PERMISSIONS_CHANGE_ANYTIME = "permissions.any.edit"

  val PERM__OPTIONS_LIST_VIEW = "options.list.view"
  val PERM__OPTIONS_EDIT = "options.edit"
  val PERM__ACCOUNTS_LIST_VIEW = "accounts.list.view"
  val PERM__ACCOUNTS_ANY_EDIT = "accounts.any.edit"
  val PERM__MENU_VIEW = "menu.view"

	val PERM__COMMENTS_CHANGE_ANYTIME = "comments.any.edit"
	val PERM__COMMENTS_CHANGE_OWN = "comments.own.change"
	val PERM__COMMENTS_CREATE_CONDITIONAL = "comments.create.conditional"
	val PERM__COMMENTS_CREATE_ANYTIME = "comments.create.anytime"

	val PERM__PROFILE_OWN_CHANGE = "profile.own.change"

	val PERM__PROFILE_ANY_CHANGE = "profile.any.change"

	def OR(names: String*): String =
    names.mkString("(",")|(",")")

  def apply(
             id: Long,
             value: String,
             descr: Option[String]): Permission =
    new Permission(
      id,
      value,
      descr)

}

object RoleTargetTypes extends Enumeration() {

  type RoleTargetTypes = Value

  val ACCOUNT = Value("account")

}

object PermissionTargetTypes extends Enumeration() {

  type PermissionTargetTypes = Value

  val ACCOUNT = Value("account")

  val ROLE = Value("role")

}

