package models.dao.slick

import controllers.AppConstants
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.ExecutionContext

trait SlickCommonDAO
	extends HasDatabaseConfigProvider[slick.jdbc.JdbcProfile] with SlickResultExtensions {

	val dbConfigProvider: DatabaseConfigProvider

	import dbConfig.profile.api._

	def pageSize(p: Int) =
		if (p > AppConstants.MAX_PAGE_SIZE) AppConstants.MAX_PAGE_SIZE
		else if (p < 1) AppConstants.MAX_PAGE_SIZE
		else p

	def dropCount(pSize: Int, pId: Int) =
		if (pId > 0) pageSize(pSize) * (pId - 1) else 0

	def pages(size: Int, pSize: Int): Int = {
		val fixedPageSize = pageSize(pSize)
		if (size == 0) 0 else {
			val fSize = size / fixedPageSize
			if (fSize * fixedPageSize < size) fSize + 1 else fSize
		}
	}

	implicit class Paginated[+E, U](query: Query[E, U, Seq]) {

		def page(pSize: Int, pId: Int): Query[E, U, Seq] =
			query
				.drop(dropCount(pSize, pId))
				.take(pageSize(pSize))

		/*def pagesCount(pSize: Int): Query[E, U, Seq] =
      query
        .size.result.map( size => pages(size, pSize) )*/

	}

	def pages(size: Int): Int = pages(size, AppConstants.DEFAULT_PAGE_SIZE)

	def maybeOptActionF[A, R](maybe: Option[A])(action: A => R)(implicit ex: ExecutionContext): DBIO[Option[R]] =
		maybe match {
			case Some(a) => DBIO.successful(Some(action(a)))
			case _ => DBIO.successful(Option.empty[R])
		}

	def isOpt[R](condition: Boolean)(f: DBIO[Option[R]]): DBIO[Option[R]] =
		if (condition) f else DBIO.successful(None)

	def maybeOptAction[A, R](maybe: Option[A])(action: A => DBIO[Option[R]])(implicit ex: ExecutionContext): DBIO[Option[R]] =
		maybe match {
			case Some(a) => action(a)
			case _ => DBIO.successful(Option.empty[R])
		}

	def maybeOptActionSeqR[A, R](maybe: Option[A])(action: A => DBIO[Seq[R]])(implicit ex: ExecutionContext): DBIO[Seq[R]] =
		maybe match {
			case Some(a) => action(a)
			case _ => DBIO.successful(Seq.empty[R])
		}

	def maybeOptAction[A, R](maybe: DBIO[Option[A]])(action: A => R)(implicit ex: ExecutionContext): DBIO[Option[R]] =
		maybe.map(_.map(action))

	def maybeOptActionSeqR[A, R](maybe: DBIO[Option[A]])(action: A => DBIO[Seq[R]])(implicit ex: ExecutionContext): DBIO[Seq[R]] =
		maybe.flatMap(_.map(action).getOrElse(DBIO.successful(Seq.empty)))

	def maybeOptActionSeq[A, R, C](maybe: Option[A])(maybe2: A => DBIO[Option[R]])(action: R => C)(implicit ex: ExecutionContext): DBIO[Option[C]] =
		maybeOptAction(maybe) { a =>
			maybeOptAction(maybe2(a)) { r => action(r) }
		}

	def maybeOptSqlActionSeq[A, R, C](maybe: DBIO[Option[A]])(maybe2: A => DBIO[Option[R]])(action: R => C)(implicit ex: ExecutionContext): DBIO[Option[C]] =
		maybe.flatMap { a => maybeOptActionSeq(a)(maybe2)(action) }

}













