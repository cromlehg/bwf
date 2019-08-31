package services

import java.io.{BufferedReader, InputStreamReader}

import play.api.Logger

object SystemHelper {

	def cmd(strCmd: Seq[String]): Seq[String] = {
		Logger.debug("Command execution:")
		Logger.debug(strCmd.mkString("\n"))
		val p = Runtime.getRuntime().exec(strCmd.toArray)
		val input = new BufferedReader(new InputStreamReader(p.getInputStream()))
		// FIXME: Does It needs to close stream
		Stream.continually(input.readLine()).takeWhile(_ != null).drop(1).toSeq
	}

	def cmd(strCmd: String): Seq[String] = {
		Logger.debug("Command execution:")
		Logger.debug(strCmd)
		val p = Runtime.getRuntime().exec(strCmd)
		val input = new BufferedReader(new InputStreamReader(p.getInputStream()))
		// FIXME: Does It needs to close stream
		Stream.continually(input.readLine()).takeWhile(_ != null).drop(1).toSeq
	}


}
