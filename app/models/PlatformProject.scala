package models

import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import services.MySQLHelper

import scala.concurrent.{ExecutionContext, Future}

case class PlatformProject(id: Long,
													 name: String,
													 userId: Long,
													 userLogin: String,
													 gitURL: String,
													 gitLogin: Option[String],
													 gitPwd: Option[String],
													 dbName: Option[String],
													 dbUser: Option[String],
													 dbPass: Option[String],
													 port: Long,
													 status: PlatformProjectStatuses.PlatformProjectStatus,
													 descr: Option[String],
													 registered: Long) {

	lazy val projectFolder: String =
		"/home/" + userLogin + "/bwf/env-" + id + "/"

	val ldt = new LocalDateTime(registered, DateTimeZone.UTC)

	val displayName = name

	lazy val createdPrettyTime =
		controllers.TimeConstants.prettyTime.format(new java.util.Date(registered))

	override def equals(obj: Any) = obj match {
		case t: PlatformProject => t.name == name
		case _ => false
	}

	def getRegistered(zone: String): DateTime = getRegistered.toDateTime(DateTimeZone forID zone)

	def getRegistered: LocalDateTime = ldt

	def loginMatchedBy(filterOpt: Option[String]): String =
		filterOpt.fold(name) { filter =>
			val start = name.indexOf(filter)
			val end = start + filter.length;
			val s = "<strong>"
			val e = "</strong>"
			if (start == 0 && end == name.length) {
				s + name + e
			} else if (start == 0 && end != name.length) {
				s + name.substring(0, end) + e + name.substring(end, name.length)
			} else if (start != 0 && end == name.length) {
				name.substring(0, start) + s + name.substring(start, name.length) + e
			} else {
				name.substring(0, start) + s + name.substring(start, end) + e + name.substring(end, name.length)
			}
		}

}

object PlatformProjectStatuses extends Enumeration {

	type PlatformProjectStatus = Value

	val STOPPED = Value("pp.stopped")

	val RUNNED = Value("pp.runned")

	val PREPARE = Value("pp.prepare")

	val CREATION = Value("pp.creation")

}

object PlatformProject {

	def usersGroup: String =
		"users"

	def dbName(envId: Long): String =
		"bwf_env_db_" + envId

	def dbUser(envId: Long): String =
		"bwf_env_user_" + envId


	def bwfPath(user: String): String =
		"/home/" + user + "/bwf"

	def ppPath(user: String, envId: Long): String =
		bwfPath(user) + "/env_" + envId

	def createDB(rootUser: String,
							 rootPass: String,
							 dbName: String,
							 dbUser: String,
							 dbPass: String)(implicit ec: ExecutionContext): Future[Either[PlatformError, Database]] = Future {
		val r = for {
			_ <- MySQLHelper.executeUpdate(rootUser, rootPass, "CREATE DATABASE " + dbName + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;")
			_ <- MySQLHelper.executeUpdate(rootUser, rootPass, "CREATE USER '" + dbUser + "'@'localhost' IDENTIFIED BY '" + dbPass + "';")
			_ <- MySQLHelper.executeUpdate(rootUser, rootPass, "GRANT ALL PRIVILEGES ON " + dbUser + ".* TO '" + dbName + "'@'localhost';")
			t <- MySQLHelper.executeUpdate(rootUser, rootPass, "FLUSH PRIVILEGES;")
		} yield t

		r match {
			case Right(_) => Right(Database(dbName))
			case Left(e) => LeftPlatformError(e)
		}
	}


	def apply(id: Long,
						name: String,
						userId: Long,
						userLogin: String,
						gitURL: String,
						gtiLogin: Option[String],
						gitPwd: Option[String],
						dbName: Option[String],
						dbUser: Option[String],
						dbPass: Option[String],
						port: Long,
						status: PlatformProjectStatuses.PlatformProjectStatus,
						descr: Option[String],
						registered: Long): PlatformProject =
		new PlatformProject(id,
			name,
			userId,
			userLogin,
			gitURL,
			gtiLogin,
			gitPwd,
			dbName,
			dbUser,
			dbPass,
			port,
			status,
			descr,
			registered)

}
