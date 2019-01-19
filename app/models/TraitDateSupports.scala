package models

trait TraitDateSupports {

  val simpleShortFormatter = new java.text.SimpleDateFormat("yyyy/MM/dd")

  def formattedShortDate(date: Long) = simpleShortFormatter.format(new java.util.Date(date))

}