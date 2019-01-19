package controllers

object AppConstants {

  val APP_NAME = "Blockwit"

  val VERSION = "0.1a"

  val BACKEND_NAME = APP_NAME + " " + VERSION

  val DEFAULT_PAGE_SIZE = 10
  
  val MAX_PAGE_SIZE = 100

  val SESSION_EXPIRE_TYME: Long = 3 * TimeConstants.DAY

  val PWD_MIN_LENGTH: Long = 12

	val SHORT_TITLE_DESCR = 50

  val DESCRIPTION_SIZE = 300

	val SHORT_DESCRIPTION_SIZE = 150

  val RETURN_URL = "referer"

}
