package models

case class MenuState(
  val activeId: String,
  val path: Seq[String]) {

  def isActive(id: String) = id == activeId

  def onThePath(id: String) = (path contains id) && (id != activeId)

}

object MenuState {

  def apply(activeId: String): MenuState =
    new MenuState(activeId, Seq(activeId))

  def apply(activeId: String, path: Seq[String]): MenuState =
    new MenuState(activeId, path)

}