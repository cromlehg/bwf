package models

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FormError, Mapping}

/**
	*
	* @author Alexander Strakh, cromlehg@gmail.com
	*
	**/
class Percent(val value: Long) {

	def applyRoundRule(in: Double) = in.toLong

	def toDouble = value.toDouble / Percent.DECIMALS_DIV

	override def toString: String = value % Percent.DECIMALS_DIV match {
		case decimals if decimals != 0 => (value / Percent.DECIMALS_DIV) + "." + decimals
		case _ => (value / Percent.DECIMALS_DIV).toString
	}

}

object Percent {

	val ZERO = Percent(0)

	val DECIMALS_DIV = 100

	implicit def percentFormat: Formatter[Percent] = new Formatter[Percent] {

		def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Percent] =
			data.get(key).map(Percent.fromString).toRight(Seq(FormError(key, "error.required", Nil)))

		def unbind(key: String, value: Percent) = Map(key -> value.toString)
	}

	val percent: Mapping[Percent] = of[Percent]

	def apply(value: Long): Percent = new Percent(value)

	def fromString(value: String): Percent =
		new Percent(value.replace(".", "").toLong)

}
