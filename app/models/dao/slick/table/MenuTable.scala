package models.dao.slick.table

trait MenuTable extends CommonTable {

  import dbConfig.profile.api._
  
  class InnerCommonTable(tag: Tag) extends Table[models.Menu](tag, "menus") with DynamicSortBySupport.ColumnSelector {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def parentId = column[Option[Long]]("parent_id")
    def menuId = column[Option[Long]]("menu_id")
    def link = column[Option[String]]("link")
    def name = column[String]("name")
    def content = column[Option[String]]("content")
    def order = column[Int]("order")
    def * = (
        id, 
        parentId,
        menuId,
        link,
        name,
        content,
        order) <> [models.Menu](t => models.Menu(
            t._1, 
            t._2,
            t._3,
            t._4,
            t._5,
            t._6,
            t._7), t => Some(
            (t.id, 
            t.parentId,
            t.menuId,
            t.link,
            t.name,
            t.content,
            t.order)))
    override val select = Map(
      "id" -> (this.id),
      "parentId" -> (this.parentId),
      "menuId" -> (this.menuId),
      "link" -> (this.link),
      "order" -> (this.order),
      "name" -> (this.name),
      "content" -> (this.content))
  }

  val table = TableQuery[InnerCommonTable]
  
}
