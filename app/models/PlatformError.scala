package models

case class PlatformError(descr: Option[String], e: Option[Exception], code: Option[Int]) {

}

object PlatformError {

	def apply(e: Exception): PlatformError =
		PlatformError(Some(e.getMessage()), Some(e), None)

	def apply(d: String): PlatformError =
		PlatformError(Some(d), None, None)

}

object LeftPlatformError {

	def apply[T](e: Exception): Left[PlatformError, T] = {
		e.printStackTrace()
		Left(PlatformError(e))
	}

	def apply[T](d: String): Left[PlatformError, T] =
		Left(PlatformError(d))

}

