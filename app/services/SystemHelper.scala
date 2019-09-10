package services

import java.io.{IOException, InputStream}
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

import models.{LeftPlatformError, PlatformError}
import org.apache.commons.io.IOUtils
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

object SystemHelper {

	def cmds(strCmd: Seq[String])(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[String]]] = Future {
		Logger.debug("Command execution:")
		Logger.debug(strCmd.mkString("\n"))

		var is: Option[InputStream] = None

		try {

			var p: Process = null;

			if (strCmd.size == 1)
				p = Runtime.getRuntime().exec(strCmd(0))
			else
				p = Runtime.getRuntime().exec(strCmd.toArray)

			try p.waitFor(5, TimeUnit.SECONDS) catch {
				case _: InterruptedException => LeftPlatformError("Timeout limit for command exceeded!")
			}

			if (p.exitValue() == 0) {
				is = Some(p.getInputStream())
				is match {
					case Some(t) => Right(IOUtils.toString(t, StandardCharsets.UTF_8).split("\n"))
					case _ => LeftPlatformError("No input stream")
				}
			} else {
				is = Some(p.getErrorStream())
				is match {
					case Some(t) => LeftPlatformError(IOUtils.toString(t, StandardCharsets.UTF_8))
					case _ => LeftPlatformError("No input stream")
				}
			}


		}

		catch {
			case e: IOException => LeftPlatformError(e)
		}
		finally is.foreach(_.close)
	}

	def cmd(strCmd: String)(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[String]]] =
		cmds(Seq(strCmd))


	def cmdsShell(strCmd: Seq[String])(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[String]]] =
		cmds(Seq("/bin/sh", "-c") ++ strCmd)

	def cmdShell(strCmd: String)(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[String]]] =
		cmdsShell(Seq(strCmd))


	def createPathRecBySUDO(path: String, sudoPass: String)(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[String]]] =
		cmdShell("echo \"" + sudoPass + "\" | sudo -S mkdir -p " + path)

	def chGroupBySUDO(path: String, sudoPass: String, group: String, isRec: Boolean)(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[String]]] =
		cmdShell("echo \"" + sudoPass + "\" | sudo chgrp " + group + " " + (if (isRec) "-R" else "") + " " + path);

	def chOwnerBySUDO(path: String, sudoPass: String, user: String, isRec: Boolean)(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[String]]] =
		cmdShell("echo \"" + sudoPass + "\" | sudo chown " + user + " " + (if (isRec) "-R" else "") + " " + path);


}
