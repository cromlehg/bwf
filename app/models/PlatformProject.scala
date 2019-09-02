package models

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

case class PlatformProject(id: Long,
													 name: String,
													 userId: Long,
													 userLogin: String,
													 gitURL: String,
													 gitLogin: String,
													 gitPwd: String,
													 dbName: String,
													 dbUser: String,
													 dbPass: String,
													 descr: Option[String],
													 registered: Long) {

	lazy val projectFolder: String =
		"/home/" + userLogin + "/bwf/env-" + id + "/"

	val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

	val displayName = name

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(registered))

	override def equals(obj: Any) = obj match {
		case t: PlatformProject => t.name == name
		case _ => false
	}

	def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

	def getRegistered: LocalDateTime = ldt

	def loginMatchedBy(filterOpt: Option[String]): String =
		filterOpt.fold(name) { filter =>
			val start = name.indexOf(filter)
			val end = start + filter.length;
			val s = "<strong>"
			val e = "</strong>"
			if (start == 0 && end == name.length) {
				s + name + e
			} else if (start == 0 && end != name.length) {
				s + name.substring(0, end) + e + name.substring(end, name.length)
			} else if (start != 0 && end == name.length) {
				name.substring(0, start) + s + name.substring(start, name.length) + e
			} else {
				name.substring(0, start) + s + name.substring(start, end) + e + name.substring(end, name.length)
			}
		}

}

object PlatformProject {

	def apply(id: Long,
						name: String,
						userId: Long,
						userLogin: String,
						gitURL: String,
						gtiLogin: String,
						gitPwd: String,
						dbName: String,
						dbUser: String,
						dbPass: String,
						descr: Option[String],
						registered: Long): PlatformProject =
		new PlatformProject(id,
			name,
			userId,
			userLogin,
			gitURL,
			gtiLogin,
			gitPwd,
			dbName,
			dbUser,
			dbPass,
			descr,
			registered)

}
