package services

import java.io.{BufferedReader, InputStreamReader}

object SystemHelper {

	def cmd(strCmd: String): Seq[String] = {
		val p = Runtime.getRuntime().exec(strCmd)
		val input = new BufferedReader(new InputStreamReader(p.getInputStream()))
		// FIXME: Does It needs to close stream
		Stream.continually(input.readLine()).takeWhile(_ != null).drop(1).toSeq
	}


}
