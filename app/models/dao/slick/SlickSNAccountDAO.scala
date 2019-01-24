package models.dao.slick

import javax.inject.{Inject, Singleton}
import models.dao.SNAccountDAO
import models.dao.slick.table.SNAccountTable
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSNAccountDAO @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
	extends SNAccountDAO with SNAccountTable with SlickCommontDAO {

	import dbConfig.profile.api._

	import scala.concurrent.Future.{successful => future}

	private val queryById = Compiled(
		(id: Rep[Long]) => table.filter(_.id === id))

	override def close: Future[Unit] =
		future(db.close())

}
