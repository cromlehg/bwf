package models

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
