package models

import play.api.Logger
import services.SystemHelper

import scala.concurrent.{ExecutionContext, Future}

case class SystemUser(login: String,
											dummyHash: String,
											UID: String,
											GUID: String,
											descrGECOS: String,
											home: String,
											shell: String) {

}

object SystemUsers {

	def list(implicit ec: ExecutionContext): Future[Either[PlatformError, Seq[SystemUser]]] =
		SystemHelper.cmd("cat /etc/passwd").map(_.map(_.map { cmdLine =>
			val splitted = cmdLine.split(":")
			SystemUser(splitted(0), splitted(1), splitted(2), splitted(3), splitted(4), splitted(5), splitted(6))
		}))

	def existsByLogin(login: String)(implicit ec: ExecutionContext): Future[Either[PlatformError, Boolean]] =
		list.map(_.map(_.find(_.login == login).isDefined))

	def createUser(login: String, password: String, operatorPassword: String)(implicit ec: ExecutionContext): Future[Either[PlatformError, String]] = {
		// passwords should be escaped
		val targetCmd = "echo \"" + operatorPassword + "\" | sudo -S useradd -m -p $(openssl passwd \"" + password + "\") \"" + login + "\""

		SystemHelper.cmdShell(targetCmd).map(_.map { r =>
			r.foreach { cmdLine =>
				Logger.debug("cmd output: " + cmdLine)
			}
			login
		})
	}


}

object SystemUser {

	def apply(login: String,
						dummyHash: String,
						UID: String,
						GUID: String,
						descrGECOS: String,
						home: String,
						shell: String): SystemUser =
		new SystemUser(login,
			dummyHash,
			UID,
			GUID,
			descrGECOS,
			home,
			shell)

}
