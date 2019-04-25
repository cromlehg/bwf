package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import be.objectify.deadbolt.scala.models.PatternType
import controllers.AuthRequestToAppContext.ac
import javax.inject.{Inject, Singleton}
import models.dao._
import models.{ConfirmationStatus, Permission, Role, Session}
import org.mindrot.jbcrypt.BCrypt
import play.Logger
import play.api.Configuration
import play.api.data.Form
import play.api.data.Forms.{boolean, email, mapping, nonEmptyText}
import play.api.i18n.Messages
import play.api.mvc.{ControllerComponents, Flash, Request, Result}
import play.twirl.api.Html
import services.{MailVerifier, Mailer, MailerResponse}
import ua.parser.Parser

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class AccountsController @Inject()(mailer: Mailer,
																	 mailVerifier: MailVerifier,
																	 cc: ControllerComponents,
																	 deadbolt: DeadboltActions,
																	 config: Configuration)(implicit ec: ExecutionContext, dap: DAOProvider)
	extends RegisterCommonAuthorizable(mailer, cc, config) with JSONSupport {

	import scala.concurrent.Future.{successful => future}

	case class ApproveData(val login: String,
												 val pwd: String,
												 val repwd: String,
												 val haveSecured: Boolean,
												 val code: String)

	val loginVerifying = nonEmptyText(3, 20).verifying("Must contain lowercase letters and digits only.", name => name.matches("[a-z0-9]{3,20}"))

	val approveForm = Form(
		mapping(
			"login" -> loginVerifying,
			"pwd" -> nonEmptyText(8, 80),
			"repwd" -> nonEmptyText(8, 80),
			"haveSecured" -> boolean.verifying(_ == true),
			"code" -> nonEmptyText)(ApproveData.apply)(ApproveData.unapply))

	case class AuthData(val email: String, val pass: String)

	sealed trait RegData {
		def email: String

		def login: String
	}

	case class RegDataUser(override val email: String,
												 override val login: String) extends RegData

	case class ForgotPasswordData(loginOrEmail: String)

	case class RecoverPasswordData(code: String,
																 login: String,
																 password: String,
																 repassword: String)

	val authForm = Form(
		mapping(
			"email" -> nonEmptyText(3, 50),
			"pass" -> nonEmptyText(8, 80))(AuthData.apply)(AuthData.unapply))

	val regFormUser = Form(
		mapping(
			"email" -> email,
			"login" -> loginVerifying)(RegDataUser.apply)(RegDataUser.unapply))

	val forgotPasswordForm = Form(
		mapping(
			"loginOrEmail" -> nonEmptyText
		)(ForgotPasswordData.apply)(ForgotPasswordData.unapply))

	val recoverPasswordForm = Form(
		mapping(
			"code" -> nonEmptyText,
			"login" -> nonEmptyText,
			"password" -> nonEmptyText(8, 80),
			"repassword" -> nonEmptyText(8, 80)
		)(RecoverPasswordData.apply)(RecoverPasswordData.unapply))

	def login = deadbolt.SubjectNotPresent()() { implicit request =>
		future(Ok(views.html.app.login(authForm)))
	}

	def logout = deadbolt.SubjectPresent()() { implicit request =>
		request.session.get(Session.TOKEN).fold(future(BadRequest("You shuld authorize before"))) { curSessionKey =>
			dap.sessions.invalidateSessionBySessionKeyAndIP(curSessionKey, request.remoteAddress) map { _ =>
				Redirect(controllers.routes.AccountsController.login).withNewSession
			}
		}
	}

	def processLogin = deadbolt.SubjectNotPresent()() { implicit request =>
		authForm.bindFromRequest.fold(formWithErrors => future(BadRequest(views.html.app.login(formWithErrors))), { authData =>
			authCheckBlock(authData.email, authData.pass) { msg =>
				val formWithErrors = authForm.fill(authData)
				future(BadRequest(views.html.app.login(formWithErrors)(Flash(formWithErrors.data) + ("error" -> msg), implicitly, implicitly)))
			} {
				case (_, _) =>
					val uri = request.session.get(AppConstants.RETURN_URL).getOrElse(routes.AppController.index.toString())
					future(Redirect(uri))
			}
		})
	}

	def processApproveRegister = deadbolt.SubjectNotPresent()() { implicit request =>
		approveForm.bindFromRequest.fold(
			formWithErrors => future(BadRequest(views.html.app.approveRegister(formWithErrors))), {
				approveData =>
					if (approveData.pwd == approveData.repwd)
						dap.accounts.findAccountOptByConfirmCodeAndLogin(approveData.login, approveData.code) flatMap (_.fold(future(BadRequest("Login or confirm code not found"))) { account =>
							dap.accounts.emailVerified(approveData.login, approveData.code, approveData.pwd) map (_.fold(BadRequest("Can't verify email")) { accountVerified =>
								Ok(views.html.app.registerFinished())
							})
						})
					else {
						val formWithErrors = approveForm.fill(approveData)
						future(Ok(views.html.app.approveRegister(formWithErrors)(Flash(formWithErrors.data) + ("error" -> "Passwords should be equals"), implicitly, implicitly)))
					}
			})
	}

	def approveRegister(login: String, code: String) = deadbolt.SubjectNotPresent()() { implicit request =>
		dap.accounts.findAccountOptByConfirmCodeAndLogin(login, code) map (_.fold(BadRequest("Login or confirm code not found")) { account =>
			Ok(views.html.app.approveRegister(approveForm.fill(ApproveData(
				login,
				BCrypt.hashpw(login.toString + code + System.currentTimeMillis() + Random.nextDouble(), BCrypt.gensalt()),
				null,
				false,
				code))))
		})
	}

	private def baseRegisterChecks[T <: RegDataUser]
	(regForm: Form[T])
	(f1: (String, Form[_]) => Future[Result])
	(f2: Form[_] => Html)
	(f3: (T, String, String) => Future[Result])
	(implicit request: Request[_], ac: AppContext) = {
		regForm.bindFromRequest.fold(
			formWithErrors => future(BadRequest(f2(formWithErrors))),
			accountInRegister => {
				val checks = for {
					isLoginExists <- dap.accounts.isLoginExists(accountInRegister.login)
					isEmailExists <- dap.accounts.isEmailExists(accountInRegister.email)
					isEmailValid <- mailVerifier.isValid(accountInRegister.email)
				} yield (isLoginExists, isEmailExists, isEmailValid)
				checks flatMap {
					case (true, _, _) =>
						f1("Login already in use.", regForm.fill(accountInRegister))
					case (_, true, _) =>
						f1("Email already in use.", regForm.fill(accountInRegister))
					case (_, _, false) =>
						f1("Incorrect Email address. Please, try another.", regForm.fill(accountInRegister))
					case _ =>
						f3(accountInRegister, accountInRegister.login, accountInRegister.email)
				}
			}
		)
	}

	def registerProcessUser() = deadbolt.SubjectNotPresent()() { implicit request =>

		def redirectWithError(msg: String, form: Form[_]) =
			future(Ok(views.html.app.registerUser(form)(Flash(form.data) + ("error" -> msg), implicitly, implicitly)))

		baseRegisterChecks(regFormUser)(redirectWithError)(t => views.html.app.registerUser(t)) { (target, login, email) =>
			createAccount("sendgrid.letter", login, email, Role.ROLE_CLIENT) { account =>
				Ok(views.html.app.registerProcess())
			}
		}

	}

	def registerUser = deadbolt.SubjectNotPresent()() { implicit request =>
		future(Ok(views.html.app.registerUser(regFormUser)))
	}

	def denied = deadbolt.WithAuthRequest()() { implicit request =>
		future(Forbidden(views.html.app.denied()))
	}

	def adminAccountsListPage = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_id")(pageIdOpt => fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.accounts.accountsListPage(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				pageIdOpt.getOrElse(0),
				Seq.empty,
				filterOpt) map { accounts =>
				Ok(views.html.admin.parts.accountsListPage(accounts))
			}
		}))
	}

	def panelProfile(accountId: Long) = deadbolt.Pattern(Permission.OR(Permission.PERM__ADMIN, Permission.PERM__CLIENT), PatternType.REGEX)() { implicit request =>
		checkedOwner(accountId, Permission.PERM__ADMIN, Permission.PERM__CLIENT) {
			dap.accounts.findAccountOptById(accountId) map (_.fold(NotFound("Account not found!")) { account =>
				Ok(views.html.admin.profile(account))
			})
		}
	}

	def adminAccountsListPagesCount = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		fieldIntOpt("page_size")(pageSizeOpt => fieldStringOpt("filter") { filterOpt =>
			dap.accounts.accountsListPagesCount(
				pageSizeOpt.getOrElse(AppConstants.DEFAULT_PAGE_SIZE),
				filterOpt) map { count => Ok(count.toString) }
		})
	}

	def adminAccounts = deadbolt.Pattern(Permission.PERM__ADMIN)() { implicit request =>
		future(Ok(views.html.admin.accounts()))
	}

	def setAccountStatus(accountId: Long, statusStr: String) = deadbolt.Pattern(Permission.PERM__ADMIN)(parse.json) { implicit request =>
		models.AccountStatus.valueOf(statusStr).fold(future(BadRequest("Wrong status " + statusStr))) { status =>
			dap.accounts.setAccountStatus(accountId, status) map { success =>
				if (success)
					(request.headers.get("referer")
						.fold(Redirect(controllers.routes.AppController.index)) { url => Redirect(url) })
						.flashing("error" -> ("New status has been set for account with id  " + accountId))
				else
					(request.headers.get("referer")
						.fold(Redirect(controllers.routes.AppController.index)) { url => Redirect(url) })
						.flashing("error" -> ("Can't set new status for account with id " + accountId))
			}
		}
	}

	protected def authCheckBlock(loginOrEmail: String, pwd: String)(error: String => Future[Result])(success: (models.Account, models.Session) => Future[Result])(implicit request: Request[_]): Future[Result] =
		dap.accounts.findAccountOptByLoginOrEmail(loginOrEmail) flatMap (_.fold(error(Messages("app.login.error"))) { account =>
			if (account.confirmationStatus == ConfirmationStatus.WAIT_CONFIRMATION)
				error("Email waiting for confirmation!")
			else if (account.accountStatus == models.AccountStatus.LOCKED)
				error("Account locked!")
			else
				account.hash.fold(error(Messages("app.login.error"))) { hash =>

					if (BCrypt.checkpw(pwd, hash)) {
						def createSession = {
							val expireTime = System.currentTimeMillis + AppConstants.SESSION_EXPIRE_TIME
							val sessionKey = java.util.UUID.randomUUID.toString + "-" + account.id

							val userAgent = request.headers.get(AppConstants.HTTP_USER_AGENT)

							val uaParsed: Option[(Option[String], Option[String])] = userAgent.map { userAgentStr =>
								val r = Parser.get.parse(userAgentStr)
								(if (r.os == null) None else Some(r.os.toString), if (r.device == null) None else Some(r.device.toString))
							}

							dap.sessions.create(
								account.id,
								request.remoteAddress,
								userAgent,
								uaParsed.flatMap(_._1),
								uaParsed.flatMap(_._2),
								sessionKey,
								System.currentTimeMillis,
								expireTime) flatMap (_.fold(future(BadRequest("Couldn't create session"))) { session =>
								val token = new String(java.util.Base64.getEncoder.encode(sessionKey.getBytes))
								success(account, session).map(_.withSession(Session.TOKEN -> token))
							})
						}

						request.session.get(Session.TOKEN).fold(createSession)(curSessionKey =>
							dap.sessions.findSessionByAccountIdSessionKeyAndIP(account.id, request.remoteAddress, curSessionKey)
								flatMap (_.fold(createSession)(session => error("You should logout before."))))

					} else error(Messages("app.login.error"))

				}

		})

	//-------------------------------------------------------------------------------------------------------------------
	// Password Recovery
	//-------------------------------------------------------------------------------------------------------------------

	private def generateRecoveryCode(login: String): String =
		BCrypt.hashpw(Random.nextString(5) + login + System.currentTimeMillis.toString, BCrypt.gensalt())
			.replaceAll("\\.", "s")
			.replaceAll("\\\\", "d")
			.replaceAll("\\$", "g")
			.toList.map(_.toInt.toHexString)
			.mkString.substring(0, 99)

	def forgotPassword = deadbolt.SubjectNotPresent()() { implicit request =>
		future(Ok(views.html.app.forgotPassword(forgotPasswordForm)))
	}

	def processForgotPassword = deadbolt.SubjectNotPresent()() { implicit request =>
		forgotPasswordForm.bindFromRequest.fold(
			formWithErrors => future(BadRequest(views.html.app.forgotPassword(formWithErrors))),
			data => dap.accounts.findAccountOptByLoginOrEmail(data.loginOrEmail) flatMap {
				case None =>
					future(Redirect(controllers.routes.AccountsController.passwordSent))
				case Some(account) if account.passwordRecoveryDate.exists(_ + 600000 > System.currentTimeMillis) =>
					future(Redirect(controllers.routes.AccountsController.passwordSent))
				case Some(account) =>
					val code = generateRecoveryCode(account.login)
					dap.accounts.generatePasswordRecoveryCode(account.id, code) flatMap { _ =>
						try {
							mailer.sendPasswordRecoveryToken(account.email, account.login, code) match {
								case MailerResponse(true, _, _) =>
									future(Redirect(controllers.routes.AccountsController.passwordSent))
								case MailerResponse(false, status, msg) =>
									Logger.error("can't send email")
									Logger.error("status code: " + status)
									Logger.error("message: " + msg)
									future(BadRequest("Some problems"))
							}
						} catch {
							case e: java.io.IOException =>
								Logger.error(e.toString)
								future(BadRequest("Some problems"))
						}
					}
			}
		)
	}

	def passwordSent = deadbolt.SubjectNotPresent()() { implicit request =>
		future(Ok(views.html.app.passwordSent()))
	}

	def recoverPassword(login: String, code: String) = deadbolt.SubjectNotPresent()() { implicit request =>
		future(Ok(views.html.app.recoverPassword(recoverPasswordForm.fill(RecoverPasswordData(code, login, null, null)))))
	}

	def processRecoverPassword = deadbolt.SubjectNotPresent()() { implicit request =>
		recoverPasswordForm.bindFromRequest.fold(
			formWithErrors => future(BadRequest(views.html.app.recoverPassword(formWithErrors))),
			data =>

				if (data.repassword != data.password)
					future(BadRequest(views.html.app.recoverPassword(recoverPasswordForm.fill(data))).flashing("error" -> "Пароли не совпадают!"))
				else
					dap.accounts.findAccountOptByLogin(data.login) flatMap {
						case None =>
							future(BadRequest(views.html.app.recoverPassword(recoverPasswordForm.fill(data))).flashing("error" -> "Аккаунт не найден"))
						case Some(account) if account.passwordRecoveryDate.forall(_ + 600000 < System.currentTimeMillis) =>
							future(BadRequest(views.html.app.recoverPassword(recoverPasswordForm.fill(data))).flashing("error" -> "Время действия проверочного кода истекло"))
						case Some(account) if !account.passwordRecoveryCode.contains(data.code) =>
							future(BadRequest(views.html.app.recoverPassword(recoverPasswordForm.fill(data))).flashing("error" -> "Неправильный проверочный код"))
						case Some(account) => dap.accounts.update(account.id, data.password) flatMap { _ =>
							dap.accounts.deletePasswordRecoveryCode(account.id) flatMap { _ =>
								future(Redirect(controllers.routes.AccountsController.login))
							}
						}
					}
		)
	}


}

