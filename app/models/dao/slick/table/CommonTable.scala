package models.dao.slick.table

import models.{CurrencyValue, Percent}
import play.api.db.slick.HasDatabaseConfigProvider

trait CommonTable extends HasDatabaseConfigProvider[slick.jdbc.JdbcProfile] {

	import dbConfig.profile.api._

	def enum2String(enum: Enumeration) = MappedColumnType.base[enum.Value, String](
		b => b.toString,
		i => enum.withName(i))

	implicit val currencyValue2Long = MappedColumnType.base[CurrencyValue, Long](
		b => b.value,
		i => CurrencyValue(i))

	implicit val percent2Long = MappedColumnType.base[Percent, Long](
		b => b.value,
		i => Percent(i))

}

