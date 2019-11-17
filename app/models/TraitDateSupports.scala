package models

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

trait TraitDateSupports {

	val registered: Long

	val simpleShortFormatter = new java.text.SimpleDateFormat("yyyy/MM/dd")

	def formattedShortDate(date: Long) = simpleShortFormatter.format(new java.util.Date(date))

	val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(registered))

	def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

	def getRegistered: LocalDateTime = ldt

}
