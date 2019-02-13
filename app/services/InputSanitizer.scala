package services

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist

/**
	* To provide sanitization for chat messages.
	*/
trait InputSanitizer {
	def sanitize(input: String): String
}

class JSoupInputSanitizer extends InputSanitizer {
	override def sanitize(input: String): String = {
		Jsoup.clean(input, Whitelist.basic())
	}
}

