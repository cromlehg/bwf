package services

import java.sql._
import java.util
import java.util.Properties

import models.Database

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object MySQLHelper {

	/**
	 * This method makes a connection to MySQL Server
	 * In this example, MySQL Server is running in the local host (so 127.0.0.1)
	 * at the standard port 3306
	 */
	private def getConnection(user: String, pwd: String): Either[String, Connection] = {
		val connectionProps = new Properties()
		connectionProps.put("user", user)
		connectionProps.put("password", pwd)
		connectionProps.put("useUnicode", "true")
		connectionProps.put("useJDBCCompliantTimezoneShift", "true")
		connectionProps.put("useJDBCCompliantTimezoneShift", "true")
		connectionProps.put("useLegacyDatetimeCode", "false")
		connectionProps.put("serverTimezone", "UTC")
		//jdbc:mysql://localhost/db?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC

		Try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/", connectionProps)
		} match {
			case Success(t) => Right(t)
			case Failure(e) =>
				e.printStackTrace()
				Left(e.getMessage)
		}

	}

	private def withConnection[T](user: String, pwd: String)(f: Connection => Either[String, T]): Either[String, T] = {
		getConnection(user, pwd).flatMap { c =>
			if (c == null)
				Left("Connection is null")
			else
				f(c).flatMap { r: T =>
					Try(c.close()) match {
						case Success(_) => Right(r)
						case Failure(e) =>
							e.printStackTrace()
							Left(e.getMessage)
					}
				}
		}
	}

	private def connWithStatement[T](c: Connection)(f: Statement => Either[String, T]): Either[String, T] =
		if (c == null)
			Left("Connection is null")
		else
			Try(c.createStatement()) match {
				case Success(s) =>
					f(s).flatMap { r =>
						Try(s.close()) match {
							case Success(_) => Right(r)
							case Failure(e) =>
								e.printStackTrace()
								Left(e.getMessage)
						}
					}
				case Failure(e) =>
					e.printStackTrace()
					Left(e.getMessage)
			}

	private def stmtWithResultSet[T](s: Statement, query: String)(f: ResultSet => Either[String, T]): Either[String, T] =
		if (s == null)
			Left("Statement is null!")
		else
			Try {
				var rs = s.executeQuery("SHOW DATABASES;")

				if (s.execute("SHOW DATABASES;")) {
					rs = s.getResultSet()
				}

				rs
			} match {
				case Success(rs) =>
					f(rs).flatMap { r =>
						Try(rs.close()) match {
							case Success(_) => Right(r)
							case Failure(e) =>
								e.printStackTrace()
								Left(e.getMessage)
						}
					}
				case Failure(e) =>
					e.printStackTrace()
					Left(e.getMessage)
			}


	private def executeQuery[T](user: String, pwd: String, query: String)(f: ResultSet => Either[String, T]): Either[String, T] =
		withConnection(user, pwd)(c => connWithStatement(c)(s => stmtWithResultSet(s, query)(f)))

	def listDatabases(user: String, pwd: String): Either[String, Seq[Database]] =
		executeQuery(user, pwd, "SHOW DATABASES;") { rs =>
			val r = new util.ArrayList[String]()
			while (rs.next()) {
				r.add(rs.getString("Database"))
			}
			Right[String, Seq[Database]](r.asScala.map(Database))
		}

}
