package models

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

case class SNAccount(val id: Long,
										 val ownerId: Long,
										 val snType: SNNAccountTypes.SNNAccountType,
										 val login: String,
										 val registered: Long) {

	val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(registered))

	override def equals(obj: Any) = obj match {
		case account: SNAccount => account.id == id
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

object SNNAccountTypes extends Enumeration {

	type SNNAccountType = Value

	val TELEGRAM = Value("telegram")

	val idToString = Seq(
		TELEGRAM -> "telegram")
		.map(t => (t._1.toString, t._2))

	def valueOf(name: String) = this.values.find(_.toString == name)

}

object SNAccount {

	def apply(id: Long,
						ownerId: Long,
						snType: SNNAccountTypes.SNNAccountType,
						login: String,
						registered: Long): SNAccount =
		new SNAccount(id,
			ownerId,
			snType,
			login,
			registered)

}
