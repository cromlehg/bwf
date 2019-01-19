package models

object SortHelper {

  def mapFromRouterString(stringOpt: Option[String]): Seq[(String, Boolean)] =
    stringOpt.fold(Seq.empty[(String, Boolean)]) { string =>
      string
        .trim
        .split("\\+")
        .map(_.split("->"))
        .filter(_.length == 2)
        .map(t => (t(0).trim, t(1).trim))
        .filter(t => t._1.matches("[a-zA-Z0-9]+") && t._2.matches("(asc)|(desc)"))
        .map { case (name, by) => (name.trim, if (by == "asc") true else false) }
        .toSeq
    }

}