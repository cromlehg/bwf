package models.dao.slick.table

import slick.lifted.Rep

object DynamicSortBySupport {

  import slick.ast.Ordering.Direction
  import slick.ast.Ordering
  import slick.lifted.Query
  import slick.lifted.ColumnOrdered
  import slick.lifted.Ordered

  type ColumnOrdering = (String, Direction) //Just a type alias

  trait ColumnSelector {
    val select: Map[String, Rep[_]] //The runtime map between string names and table columns
  }

  implicit class MultiSortableQuery[A <: ColumnSelector, B](query: Query[A, B, Seq]) {
    def dynamicSortBy(sortBy: Seq[ColumnOrdering]): Query[A, B, Seq] =
      sortBy.foldRight(query) { //Fold right is reversing order
        case ((sortColumn, sortOrder), queryToSort) =>
          val sortOrderRep: Rep[_] => Ordered = ColumnOrdered(_, Ordering(sortOrder))
          val sortColumnRep: A => Rep[_] = _.select(sortColumn)
          queryToSort.sortBy(sortColumnRep)(sortOrderRep)
      }
  }

}
