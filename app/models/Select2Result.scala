package models

import play.api.libs.json.JsValue

object Select2Result {

  def apply(
             results: JsValue,
             pagination: Boolean): String =
    "{ \"results\": " + results + ", \"pagination\": " + pagination + "}"

}