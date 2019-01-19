package services

import scala.collection.mutable.Stack

sealed class Lex(val start: Int, val end: Int)

case class EscapedDogLex(override val start: Int, override val end: Int) extends Lex(start, end)

case class StartShortcodeLex(override val start: Int, override val end: Int) extends Lex(start, end)

case class ShortcodeLex(override val start: Int, override val end: Int) extends Lex(start, end)

case class ContentLex(override val start: Int, override val end: Int) extends Lex(start, end)

object ShortcodesFastParser {

	val SMBL_START_SHORTCODE = '@'

	val SMBL_START_SHORTCODE_CONTENT = '{'

	val SMBL_END_SHORTCODE_CONTENT = '}'

	def parse(content: String): Seq[Lex] = {

		val tokens = Stack[(Char, Int)]()

		val lexes = Stack[Lex]()

		content.toCharArray().zipWithIndex.map {
			case (char, pos) =>
				if (tokens.isEmpty)
					tokens.push((char, pos))
				else
					char match {
						case SMBL_START_SHORTCODE =>
							lexes.headOption match {
								case Some(StartShortcodeLex(start, end)) =>
									tokens.push((char, pos))
								case _ =>
									tokens.headOption match {
										case Some((SMBL_START_SHORTCODE, lastPos)) =>
											tokens.pop
											val prevLexEndIndex = lexes.headOption.map(_.end).getOrElse(0)
											if (lastPos - prevLexEndIndex > 0)
												lexes.push(ContentLex(prevLexEndIndex, lastPos))
											lexes.push(EscapedDogLex(lastPos, pos + 1))
										case _ =>
											tokens.push((char, pos))
									}
							}
						case SMBL_START_SHORTCODE_CONTENT =>
							lexes.headOption match {
								case Some(StartShortcodeLex(start, end)) =>
									tokens.push((char, pos))
								case _ =>
									tokens.headOption match {
										case Some((SMBL_START_SHORTCODE, lastPos)) =>
											tokens.pop
											lexes.push(StartShortcodeLex(lastPos, pos))
										case _ =>
											tokens.push((char, pos))
									}
							}
						case SMBL_END_SHORTCODE_CONTENT =>
							lexes.headOption match {
								case Some(StartShortcodeLex(start, end)) =>
									lexes.pop
									val prevLexEndIndex = lexes.headOption.map(_.end).getOrElse(0)
									if (start - prevLexEndIndex > 0)
										lexes.push(ContentLex(prevLexEndIndex, start))
									lexes.push(ShortcodeLex(start, pos + 1))
								case _ =>
									tokens.push((char, pos))
							}
						case _ =>
							tokens.push((char, pos))
					}
		}

		val prevLexEndIndex = lexes.headOption.map(_.end).getOrElse(0)
		if (content.length - prevLexEndIndex > 0)
			lexes.push(ContentLex(prevLexEndIndex, content.length))

		lexes
			.map {
				case StartShortcodeLex(start, end) => ContentLex(start, end)
				case t => t
			}
			.toList
			.sortBy(_.start)

	}

}
/*
object Parser2 extends App {

	val content1 = """
<svg  xmlns="http://www.w3.org/2000/svg"
      xmlns:xlink="http://www.w3.org/1999/xlink">
    <rect x="10" y="10" height="100" width="100"
          style="stroke:#ff0000; fill: #0000ff"/>
@{once}
</svg>
"""

	val content2 = """@{ @{ @{ @{"""

	ShortcodesFastParser.parse(content2).map { lex =>
		println(lex)
	}

}
*/
