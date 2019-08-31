package models

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

case class PlatformUser(id: Long,
												login: String,
												systemStatus: PlatformUserSystemStatuses.PlatformUserSystemStatus,
												creatorId: Long,
												registered: Long) {

	val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

	val displayName = login

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(registered))

	override def equals(obj: Any) = obj match {
		case account: PlatformUser => this.login == login
		case _ => false
	}

	def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

	def getRegistered: LocalDateTime = ldt

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

object PlatformUserSystemStatuses extends Enumeration {

	type PlatformUserSystemStatus = Value

	val RESOLVED = Value("pus.resolved")

	val NOT_IN_SYSTEM = Value("pus.not_in_system")

}

object PlatformUser {

	def apply(id: Long,
						login: String,
						systemStatus: PlatformUserSystemStatuses.PlatformUserSystemStatus,
						creatorId: Long,
						registered: Long): PlatformUser =
		new PlatformUser(id,
			login,
			systemStatus,
			creatorId,
			registered)

}
