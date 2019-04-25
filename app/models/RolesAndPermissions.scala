package models

case class Role(val id: Long,
								override val name: String,
								descr: Option[String],
								permissions: Seq[Permission])
	extends be.objectify.deadbolt.scala.models.Role {

	override def equals(obj: Any): Boolean =
		obj.isInstanceOf[Role] && obj.equals(name)

	override def toString: String = name

}

case class Permission(val id: Long,
											override val value: String,
											descr: Option[String])
	extends be.objectify.deadbolt.scala.models.Permission {

	override def equals(obj: Any): Boolean =
		obj.isInstanceOf[Permission] && obj.equals(value)

	override def toString: String = value

}

object Role {

	val ROLE_ADMIN = "admin"

	val ROLE_CLIENT = "client"

	def apply(id: Long,
						name: String,
						descr: Option[String]): Role = new Role(id,
		name,
		descr,
		Seq.empty)

	def apply(id: Long,
						name: String,
						descr: Option[String],
						permissions: Seq[Permission]): Role = new Role(id,
		name,
		descr,
		permissions)


}

object Permission {

	val PERM__ADMIN = "ref.admin"
	val PERM__CLIENT = "ref.client"

	def OR(names: String*): String =
		names.mkString("(", ")|(", ")")

	def apply(id: Long,
						value: String,
						descr: Option[String]): Permission = new Permission(id,
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

