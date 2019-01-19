package models.dao.slick

import java.sql.BatchUpdateException
import java.sql.SQLIntegrityConstraintViolationException

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import scala.util.Failure
import scala.util.Success

import slick.jdbc.MySQLProfile.api.DBIO
import slick.jdbc.MySQLProfile.api.Query
import slick.jdbc.MySQLProfile.api.anyOptionExtensionMethods
import slick.jdbc.MySQLProfile.api.booleanColumnType
import slick.jdbc.MySQLProfile.api.streamableQueryActionExtensionMethods
import slick.lifted.Rep
import slick.lifted.CanBeQueryCondition

trait SlickResultExtensions {
  implicit class DBIOActionExtensions[T](action: DBIO[T]) {
    def handleIntegrityErrors(error: Throwable)(implicit ec: ExecutionContext): DBIO[T] = {
      action.asTry.flatMap {
        case Success(i) =>
          DBIO.successful(i)
        case Failure(e: SQLIntegrityConstraintViolationException) =>
          DBIO.failed(error)
        case Failure(e: BatchUpdateException) if e.getCause.isInstanceOf[SQLIntegrityConstraintViolationException] =>
          DBIO.failed(error)
        case Failure(e) =>
          DBIO.failed(e)
      }
    }
  }

  implicit class DbioUpdateActionExtensions(action: DBIO[Int]) {
    def handleSingleUpdateError(result: Throwable)(implicit ec: ExecutionContext): DBIO[Unit] = {
      action.flatMap {
        case c if c == 1 =>
          DBIO.successful(())
        case c if c == 0 =>
          DBIO.failed(result)
        case _ =>
          DBIO.failed(new Exception("Too many elements"))
      }
    }
  }

  implicit class DBIOOptionOps[T](io: DBIO[Option[T]]) {
    def failIfNone(t: Throwable)(implicit ec: ExecutionContext): DBIO[T] =
      io.flatMap(_.fold[DBIO[T]](DBIO.failed(t))(DBIO.successful))
  }

  implicit class QueryOps[+E, U](query: Query[E, U, Seq]) {
    def resultHead(onEmpty: Throwable)(implicit ec: ExecutionContext): DBIO[U] =
      DBIOOptionOps(query.take(1).result.headOption).failIfNone(onEmpty)

    def maybeFilter(f: E => Rep[Option[Boolean]]): Query[E, U, Seq] =
      query.withFilter { (e: E) =>
        f(e).getOrElse(true)
      }

    def filterOpt[D, T <: Rep[_]: CanBeQueryCondition](option: Option[D])(f: (E, D) => T): Query[E, U, Seq] =
      option.map(d => query.filter(a => f(a, d))).getOrElse(query)

    def filterIf(p: Boolean)(f: E => Rep[Boolean]): Query[E, U, Seq] =
      if (p) query.filter(f) else query
  }

  implicit class DBIOSeqOps[+T](io: DBIO[Seq[T]]) {
    def failIfMany(implicit ec: ExecutionContext): DBIO[Option[T]] =
      io.flatMap { result =>
        if (result.size > 1)
          DBIO.failed(new Exception("Too many elements"))
        else
          DBIO.successful(result.headOption)
      }

    def failIfNotSingle(t: Throwable)(implicit ec: ExecutionContext): DBIO[T] =
      DBIOOptionOps(failIfMany).failIfNone(t)

    def failIfEmpty(t: Throwable)(implicit ec: ExecutionContext): DBIO[Seq[T]] = {
      io.flatMap { result =>
        if (result.isEmpty)
          DBIO.failed(t)
        else
          DBIO.successful(result)
      }
    }
  }
}

