package models

import be.objectify.deadbolt.scala.models.Subject
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import play.api.libs.json.{Json, Writes}

case class Account(
										val id: Long,
										val login: String,
										val email: String,
										val hash: Option[String],
										val confirmationStatus: ConfirmationStatus.ConfirmationStatus,
										val accountStatus: AccountStatus.AccountStatus,
										val registered: Long,
										val confirmCode: Option[String],
										val passwordRecoveryCode: Option[String],
										val passwordRecoveryDate: Option[Long],
										override val roles: List[Role],
										val targetPermissions: List[Permission],
										val sessionOpt: Option[Session],
										val avatar: Option[String]) extends Subject {

	override val identifier = login

	val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

	val displayName = login

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(registered))

	override def equals(obj: Any) = obj match {
		case account: Account => account.email == email
		case _ => false
	}

	val rolesPermissions: List[Permission] = roles.map(_.permissions).flatten.distinct

	override val permissions: List[Permission] = targetPermissions ++ rolesPermissions

	override def toString = email

	def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

	def getRegistered: LocalDateTime = ldt

	def containsPermission(name: String) = permissions.map(_.value).contains(name)

	def loginMatchedBy(filterOpt: Option[String]): String =
		filterOpt.fold(login) { filter =>
			val start = login.indexOf(filter)
			val end = start + filter.length;
			val s = "<strong>"
			val e = "</strong>"
			if (start == 0 && end == login.length) {
				s + login + e
			} else if (start == 0 && end != login.length) {
				s + login.substring(0, end) + e + login.substring(end, login.length)
			} else if (start != 0 && end == login.length) {
				login.substring(0, start) + s + login.substring(start, login.length) + e
			} else {
				login.substring(0, start) + s + login.substring(start, end) + e + login.substring(end, login.length)
			}
		}

}

object ConfirmationStatus extends Enumeration() {

	type ConfirmationStatus = Value

	val WAIT_CONFIRMATION = Value("wait confirmation")

	val CONFIRMED = Value("confirmed")

}

object AccountStatus extends Enumeration {

	type AccountStatus = Value

	val NORMAL = Value("normal")

	val LOCKED = Value("locked")

	def valueOf(name: String) = this.values.find(_.toString == name)

	def isAccountStatus(s: String) = values.exists(_.toString == s)

}

object Account {

	implicit lazy val accountsAdminWrites = new Writes[Account] {
		def writes(target: Account) = Json.obj(
			"id" -> target.id,
			"login" -> target.login,
			"email" -> target.email,
			"confirmation_status" -> target.confirmationStatus,
			"account_status" -> target.accountStatus,
			"regietered" -> target.createdPrettyTime)
	}

	implicit lazy val accountsForCommentsWrites = new Writes[Account] {
		def writes(target: Account) = Json.obj(
			"id" -> target.id,
			"login" -> target.login,
			"email" -> target.email,
			"regietered" -> target.createdPrettyTime)
	}

	def apply(id: Long,
						login: String,
						email: String,
						hash: Option[String],
						confirmationStatus: ConfirmationStatus.ConfirmationStatus,
						accountStatus: AccountStatus.AccountStatus,
						registered: Long,
						confirmCode: Option[String],
						passwordRecoveryCode: Option[String],
						passwordRecoveryDate: Option[Long],
						roles: List[models.Role],
						targetPermissions: List[models.Permission],
						sessionOpt: Option[Session]): Account =
		new Account(id,
			login,
			email,
			hash,
			confirmationStatus,
			accountStatus,
			registered,
			confirmCode,
			passwordRecoveryCode,
			passwordRecoveryDate,
			roles,
			targetPermissions,
			sessionOpt,
			None)

	def apply(id: Long,
						login: String,
						email: String,
						hash: Option[String],
						confirmationStatus: ConfirmationStatus.ConfirmationStatus,
						accountStatus: AccountStatus.AccountStatus,
						registered: Long,
						confirmCode: Option[String],
						passwordRecoveryCode: Option[String],
						passwordRecoveryDate: Option[Long]): Account =
		new Account(id,
			login,
			email,
			hash,
			confirmationStatus,
			accountStatus,
			registered,
			confirmCode,
			passwordRecoveryCode,
			passwordRecoveryDate,
			List.empty[models.Role],
			List.empty[models.Permission],
			None,
			None)

}
