package models

case class BOption(id: Long,
									 name: String,
									 value: String,
									 ttype: String,
									 descr: String) extends TraitModel {

	def toBoolean = value.toBoolean

	def toOptInt: Option[Int] =
		if (value.trim.isEmpty)
			None
		else
			try Some(value.trim.toInt) catch {
				case _: Throwable => None
			}

	def toOptLong: Option[Long] =
		if (value.trim.isEmpty)
			None
		else
			try Some(value.trim.toLong) catch {
				case _: Throwable => None
			}

}

object TOption {

	def apply(id: Long,
						name: String,
						value: String,
						ttype: String,
						descr: String): BOption =
		BOption(
			id,
			name,
			value,
			ttype,
			descr)

}

object Options {

	val TYPE_BOOLEAN = "Boolean"

	val REGISTER_ALLOWED = "REGISTER_ALLOWED"

	val POSTS_CHANGE_ALLOWED = "POSTS_CHANGE_ALLOWED"

	val POSTS_CREATE_ALLOWED = "POSTS_CREATE_ALLOWED"

	val AFTER_PAGE_SCRIPT = "AFTER_PAGE_SCRIPT"

	val MAIN_MENU_ID = "MAIN_MENU_ID"

	val INDEX_PAGE_ID = "INDEX_PAGE_ID"

}
