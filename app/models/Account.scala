package models

import be.objectify.deadbolt.scala.models.Subject
import play.api.libs.json.{Json, Writes}

case class Account(id: Long,
									 login: String,
									 email: String,
									 hash: Option[String],
									 confirmationStatus: ConfirmationStatus.ConfirmationStatus,
									 accountStatus: AccountStatus.AccountStatus,
									 override val registered: Long,
									 confirmCode: Option[String],
									 passwordRecoveryCode: Option[String],
									 passwordRecoveryDate: Option[Long],
									 override val roles: List[Role],
									 targetPermissions: List[Permission],
									 sessionOpt: Option[Session],
									 balance: CurrencyValue,
									 avatar: Option[String],
									 snAccounts: Seq[SNAccount]) extends Subject with TraitDateSupports with TraitModel {

	override val identifier = login

	val isAdmin = roles.map(_.name).contains(Role.ROLE_ADMIN)

	val isWriter = roles.map(_.name).contains(Role.ROLE_WRITER)

	val isEditor = roles.map(_.name).contains(Role.ROLE_EDITOR)

	val isClient = roles.map(_.name).contains(Role.ROLE_CLIENT)

	val notAdmin = !isAdmin

	val displayName = login

	override def equals(obj: Any) = obj match {
		case account: Account => account.email == email
		case _ => false
	}

	def accountStatusHRId: String = AccountStatus.accountStatusHRId(accountStatus)

	val rolesPermissions: List[Permission] = roles.map(_.permissions).flatten.distinct

	override val permissions: List[Permission] = targetPermissions ++ rolesPermissions

	override def toString = email

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

	def accountStatusHRId(ac: AccountStatus) =
		ac match {
			case NORMAL => "admin.profile.account.status.normal"
			case LOCKED => "admin.profile.account.status.locked"
			case _ => "admin.profile.account.status.unknown"
		}

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
						sessionOpt: Option[Session],
						balance: CurrencyValue,
						snAccounts: Seq[SNAccount]): Account =
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
			balance,
			None,
			snAccounts)

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
						balance: CurrencyValue): Account =
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
			balance,
			None,
			Seq.empty)

}
