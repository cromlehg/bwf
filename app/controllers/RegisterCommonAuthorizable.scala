package controllers

import javax.inject.Inject
import models.dao.{AccountDAO, OptionDAO}
import play.Logger
import play.api.Configuration
import play.api.mvc.{ControllerComponents, Result}
import services.{Mailer, MailerResponse}

import scala.concurrent.{ExecutionContext, Future}

class RegisterCommonAuthorizable @Inject()(mailer: Mailer,
																					 cc: ControllerComponents,
																					 accountDAO: AccountDAO,
																					 optionDAO: OptionDAO,
																					 config: Configuration)(implicit ec: ExecutionContext)
	extends CommonAbstractController(optionDAO, cc) with JSONSupport {

	protected def createAccount(
															 emailPatternName: String,
															 login: String,
															 email: String,
															 role: String)(
															 f: models.Account => Result): Future[Result] = {

		for {
			account <- accountDAO.createAccountWithRole(
				login,
				email,
				role)
		} yield {

			try {
				mailer.sendVerificationToken(account.email, account.login, account.confirmCode.get) match {
					case MailerResponse(true, _, _) =>
						f(account)
					case MailerResponse(false, status, msg) =>
						Logger.error("can't send email")
						Logger.error("status code: " + status)
						Logger.error("message: " + msg)
						BadRequest("Some problems")
				}

			} catch {
				case e: java.io.IOException =>
					Logger.error(e.toString())
					BadRequest("Seom problems")
			}

		}

	}


}
