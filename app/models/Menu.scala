package models

case class Menu(id: Long,
								parentId: Option[Long],
								menuId: Option[Long],
								link: Option[String],
								name: String,
								content: Option[String],
								order: Int,
								parent: Option[Menu],
								childs: Seq[Menu]) extends TraitModel {

	def assembly(items: Seq[Menu]): Menu =
		copy(
			childs = items
				.filter(_.parentId == Some(id))
				.map(_.assembly(items).copy(parent = Some(this))).sortBy(_.order))

	override def toString =
		"Menu(name = \"" + name + "\", link = \"" +
			link.getOrElse("#") + "\", childs(" +
			childs.map(_.toString).mkString(", ") + "))"

}

object Menu {

	def apply(id: Long,
						parentId: Option[Long],
						menuId: Option[Long],
						link: Option[String],
						name: String,
						content: Option[String],
						order: Int): Menu =
		new Menu(
			id,
			parentId,
			menuId,
			link,
			name,
			content,
			order,
			None,
			Seq.empty)

	def apply(id: Long,
						parentId: Option[Long],
						menuId: Option[Long],
						link: Option[String],
						name: String,
						content: Option[String],
						order: Int,
						parent: Option[Menu],
						childs: Seq[Menu]): Menu =
		new Menu(
			id,
			parentId,
			menuId,
			link,
			name,
			content,
			order,
			parent,
			childs)

}
