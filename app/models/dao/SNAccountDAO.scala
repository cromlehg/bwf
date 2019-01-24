package models.dao

import scala.concurrent.Future

import javax.inject.Inject
import play.api.inject.ApplicationLifecycle
import models.SNAccount

trait SNAccountDAO {

  def close: Future[Unit]

}

class SNAccountDAOCloseHook @Inject() (dao: SNAccountDAO, lifecycle: ApplicationLifecycle) {
  lifecycle.addStopHook { () =>
    Future.successful(dao.close)
  }
}
