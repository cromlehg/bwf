package controllers

object AppConstants {

  val APP_NAME = "BlockWit"

  val VERSION = "0.1a"

  val BACKEND_NAME = APP_NAME + " " + VERSION

  val DEFAULT_PAGE_SIZE = 10

  val MAX_PAGE_SIZE = 100

  val SESSION_EXPIRE_TIME: Long = 3 * TimeConstants.DAY

  val PWD_MIN_LENGTH: Long = 12

	val SHORT_TITLE_DESCR = 50

  val DESCRIPTION_SIZE = 300

	val SHORT_DESCRIPTION_SIZE = 150

  val RETURN_URL = "referer"

	val HTTP_USER_AGENT = "User-Agent"

	val CHAT_MSGS_LIMIT = 5

}
