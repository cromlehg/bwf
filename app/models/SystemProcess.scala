package models

import services.SystemHelper

case class SystemProcess(UID: String,
												 PID: String,
												 PPID: String,
												 C: String,
												 STIME: String,
												 TTY: String,
												 TIME: String,
												 CMD: String) {

}

object SystemProcesses {

	val pFEWPattern = """(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*?)\s+(.*)""".r

	def list: Seq[SystemProcess] =
		SystemHelper.cmd("ps -few").flatMap(_ match {
			case pFEWPattern(uid, pid, ppid, c, stime, tty, time, cmd) =>
				Some(SystemProcess(uid, pid, ppid, c, stime, tty, time, cmd))
			case _ => None
		})

}

object SystemProcess {

	def apply(UID: String,
						PID: String,
						PPID: String,
						C: String,
						STIME: String,
						TTY: String,
						TIME: String,
						CMD: String) =
		new SystemProcess(UID,
			PID,
			PPID,
			C,
			STIME,
			TTY,
			TIME,
			CMD)

}
