package models

import controllers.AppConstants

object ErrCodes {

  val EC_EMAIL_NOT_VERIFIED = 0

  val STR_EMAIL_NOT_VERIFIED = "You should confirm registration by email"

  val EC_PASSWORD_NOT_SET = 1

  val STR_PASSWORD_NOT_SET = "Password not set"

  val EC_PASSWORD_MIN_LENGTH = 2

  val STR_PASSWORD_MIN_LENGTH = "Password length should be more than " + AppConstants.PWD_MIN_LENGTH + " symbols"

}