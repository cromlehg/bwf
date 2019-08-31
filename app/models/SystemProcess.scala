package models

case class SystemProcess(UID: String,
												 PID: String,
												 PPID: String,
												 C: String,
												 STIME: String,
												 TTY: String,
												 TIME: String,
												 CMD: String) {

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

	def parse_FEW(str: String) =
		str.split('\t')

}
