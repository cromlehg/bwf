package controllers

import org.ocpsoft.prettytime.PrettyTime

object TimeConstants {

  val prettyTime = new PrettyTime()

  val MILLISECONDS_IN_SECOND: Long = 1000L

  val SECOND: Long = MILLISECONDS_IN_SECOND

  val SECONDS_IN_MINUTE: Long = 60L

  val MINUTE: Long = SECONDS_IN_MINUTE * SECOND

  val MINUTES_IN_HOUR: Long = 60L

  val HOUR: Long = MINUTES_IN_HOUR * MINUTE

  val HOURS_IN_DAY: Long = 24L

  val DAY: Long = HOURS_IN_DAY * HOUR

  val DAYS_IN_WEEK: Long = 7L

  val WEEK: Long = DAYS_IN_WEEK * DAY

  val WEEK_IN_MONTH: Long = 4L

  val MONTH: Long = WEEK_IN_MONTH * WEEK

  val MONTH_IN_YEAR: Long = 12L

  val YEAR: Long = MONTH_IN_YEAR * MONTH

}