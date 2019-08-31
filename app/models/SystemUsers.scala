package models

import play.api.Logger
import services.SystemHelper

case class SystemUser(login: String,
											dummyHash: String,
											UID: String,
											GUID: String,
											descrGECOS: String,
											home: String,
											shell: String) {

}

object SystemUsers {

	def list: Seq[SystemUser] =
		SystemHelper.cmd("cat /etc/passwd").map { cmdLine =>
			val splitted = cmdLine.split(":")
			SystemUser(splitted(0), splitted(1), splitted(2), splitted(3), splitted(4), splitted(5), splitted(6))
		}

	def existsByLogin(login: String): Boolean =
		list.find(_.login == login).isDefined

	def createUser(login: String, password: String, operatorPassword: String) = {
		// passwords should be escaped
		val targetCmd = "echo \"" + operatorPassword + "\" | sudo -S useradd -m -p $(openssl passwd \"" + password + "\") \"" + login + "\""
		val cmds = Array[String]("/bin/sh", "-c", targetCmd)

		val result = SystemHelper.cmd(cmds)
		result.foreach { cmdLine =>
			Logger.debug("cmd output: " + cmdLine)
		}

		true
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
