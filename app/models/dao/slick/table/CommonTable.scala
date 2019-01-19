package models.dao.slick.table

import play.api.db.slick.HasDatabaseConfigProvider

trait CommonTable extends HasDatabaseConfigProvider[slick.jdbc.JdbcProfile] {

  import dbConfig.profile.api._

  def enum2String(enum: Enumeration) = MappedColumnType.base[enum.Value, String](
    b => b.toString,
    i => enum.withName(i))

}

