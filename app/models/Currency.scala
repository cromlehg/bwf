package models

import java.math.BigDecimal

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FormError, Mapping}
import play.api.libs.json.{JsValue, Json}

/**
	*
	* @author Alexander Strakh, cromlehg@gmail.com
	*
	**/

class Currency(val id: Long,
							 val ticker: String,
							 val name: String) {

}

class CurrencyValue(val value: Long) {

	def isNegative = value < 0

	def isPositive = value > 0

	def toJson()(implicit ac: controllers.SessionContext): JsValue =
		Json.toJson(value)

	def negate: CurrencyValue =
		CurrencyValue(-value)

	def >(that: CurrencyValue): Boolean =
		value > that.value

	def <(that: CurrencyValue): Boolean =
		value < that.value

	def >=(that: CurrencyValue): Boolean =
		value >= that.value

	def <=(that: CurrencyValue): Boolean =
		value <= that.value

	def >(that: Int): Boolean =
		value > that

	def <(that: Int): Boolean =
		value < that

	def >=(that: Int): Boolean =
		value >= that

	def <=(that: Int): Boolean =
		value <= that

	def ==(that: Int): Boolean =
		value == that

	def ==(that: CurrencyValue): Boolean =
		value == that.value

	def /(that: Long): CurrencyValue =
		new CurrencyValue(applyRoundRule(value / that))

	def /(that: CurrencyValue): CurrencyValue =
		new CurrencyValue(applyRoundRule(value / that.value))

	def /(that: Int): CurrencyValue =
		new CurrencyValue(applyRoundRule(value / that))

	def *(that: Long): CurrencyValue =
		new CurrencyValue(applyRoundRule(value * that))

	def *(that: Double): CurrencyValue =
		new CurrencyValue(applyRoundRule(value * that))

	def *(that: Percent): CurrencyValue = this * that.toDouble

	def *(that: CurrencyValue): CurrencyValue = CurrencyValue(value * that.value)

	def +(that: Int): CurrencyValue =
		new CurrencyValue(value + that)

	def -(that: Int): CurrencyValue =
		new CurrencyValue(value - that)

	def +(that: CurrencyValue): CurrencyValue =
		new CurrencyValue(value + that.value)

	def -(that: CurrencyValue): CurrencyValue =
		new CurrencyValue(value - that.value)

	def applyRoundRule(in: Double) = in.toLong

	def toDouble = value.toDouble / CurrencyValue.DECIMALS_DIV

	override def toString: String =
		value % CurrencyValue.DECIMALS_DIV match {
			case decimals if decimals > 9 => (value / CurrencyValue.DECIMALS_DIV) + "." + decimals
			case decimals if decimals != 0 => (value / CurrencyValue.DECIMALS_DIV) + ".0" + decimals
			case _ => (value / CurrencyValue.DECIMALS_DIV).toString
		}

	def withDelimeters: String = {
		val orig = toString
		if (orig.equals("."))
			orig
		else {
			val r = orig.split("\\.")
			if (r.length == 1) {
				val formatter = java.text.NumberFormat.getIntegerInstance
				if (orig.startsWith(".")) {
					orig
				} else {
					formatter.format(r(0).toLong)
				}
			} else if (r.length == 2) {
				val formatter = java.text.NumberFormat.getIntegerInstance
				val intPart = if (r(0).length == 0) "" else formatter.format(r(0).toLong)
				intPart + "." + r(1)
			} else {
				val formatter = java.text.NumberFormat.getIntegerInstance
				formatter.format(orig.toLong)
			}
		}
	}

}

object CurrencyValue {

	val ZERO = new CurrencyValue(0)

	val regex = """^\d*(\.\d{0,2})?$""".r

	val DECIMALS_DIV = 100

	val BD_MUL = new BigDecimal(DECIMALS_DIV)

	implicit val numeric: Numeric[CurrencyValue] = new Numeric[CurrencyValue] {
		override def plus(x: CurrencyValue, y: CurrencyValue): CurrencyValue = new CurrencyValue(x.value + y.value)

		override def minus(x: CurrencyValue, y: CurrencyValue): CurrencyValue = new CurrencyValue(x.value - y.value)

		override def times(x: CurrencyValue, y: CurrencyValue): CurrencyValue = new CurrencyValue(x.value * y.value)

		override def negate(x: CurrencyValue): CurrencyValue = new CurrencyValue(-x.value)

		override def fromInt(x: Int): CurrencyValue = new CurrencyValue(x)

		override def toInt(x: CurrencyValue): Int = x.value.toInt

		override def toLong(x: CurrencyValue): Long = x.value.toLong

		override def toFloat(x: CurrencyValue): Float = x.value.toFloat

		override def toDouble(x: CurrencyValue): Double = x.value.toDouble

		override def compare(x: CurrencyValue, y: CurrencyValue): Int = x.value.compare(y.value)
	}

	implicit def currencyValueFormat: Formatter[CurrencyValue] = new Formatter[CurrencyValue] {

		def bind(key: String, data: Map[String, String]): Either[Seq[FormError], CurrencyValue] =
			data.get(key).map(CurrencyValue.fromString).toRight(Seq(FormError(key, "error.required", Nil)))

		def unbind(key: String, value: CurrencyValue) = Map(key -> value.toString)

	}

	val currencyValue: Mapping[CurrencyValue] = of[CurrencyValue]

	def apply(value: Long): CurrencyValue = new CurrencyValue(value)

	def fromString(invalue: String): CurrencyValue = {
		val value = invalue.trim.replace(" ", "")
		if (regex.pattern.matcher(value).matches()) {
			if (value.contains(".")) {
				val splitted = value.split('.')
				if (splitted.length == 0)
					new CurrencyValue(0)
				else
					new CurrencyValue(new BigDecimal(value).multiply(BD_MUL).longValue)
			} else
				new CurrencyValue(value.toLong * DECIMALS_DIV)
		} else
			throw new NumberFormatException()
	}

}
