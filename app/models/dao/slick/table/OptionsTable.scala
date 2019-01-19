package models.dao.slick.table

trait OptionTable extends CommonTable {

  import dbConfig.profile.api._

  class InnerCommonTable(tag: Tag) extends Table[models.BOption](tag, "options") with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def value = column[String]("value")
    def ttype = column[String]("type")
    def descr = column[String]("descr")
    def * = (id, name, value, ttype, descr) <> [models.BOption](t =>
      models.BOption(t._1, t._2, t._3, t._4, t._5), models.BOption.unapply)
    override val select = Map(
      "id" -> (this.id),
      "name" -> (this.name),
      "value" -> (this.value),
      "ttype" -> (this.ttype),
      "descr" -> (this.descr))
  }

  val table = TableQuery[InnerCommonTable]
  
}
