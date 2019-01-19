package services

import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import net.sargue.mailgun.{Configuration, Mail}
import play.api.Logger

trait Mailer {

  def sendVerificationToken(to: String, login: String, code: String): MailerResponse

  def sendPasswordRecoveryToken(to: String, login: String, code: String): MailerResponse

  def sendDebugInfo(subject: String, content: String): MailerResponse

}

case class MailerResponse(val isOk: Boolean, val status: Int, val msg: String)

@Singleton
class MailGunMailer @Inject()(config: Config) extends Mailer {

  val isDummyLetters = config.getBoolean("bwf.debug.letters.dummy")

  val isLogLettersToConsole = config.getBoolean("bwf.debug.letters.toconsole")

  val isDummyDebugLetters = config.getBoolean("bwf.debug.dbg.letters.dummy")

  val isLogDebugLettersToConsole = config.getBoolean("bwf.debug.dbg.letters.toconsole")

  val debugInfoTo = config.getString("bwf.debug.dbg.to")

  val configuration = new Configuration()
    .domain(config.getString("mailer.domain"))
    .apiKey(config.getString("mailer.apikey"))
    .from(config.getString("mailer.fromname"), config.getString("mailer.from"))

  override def sendVerificationToken(to: String, login: String, code: String): MailerResponse = {

    val subject = config.getString("mailer.confirm.subject")
    val mailPattern = config.getString("mailer.confirm.pattern")

    val filledPattern = mailPattern
      .replace("%account.login%", login)
      .replace("%account.confirmCode%", code)

    if (isLogLettersToConsole)
      Logger.debug(filledPattern)

    if (isDummyLetters) {
      MailerResponse(true, 200, "")
    } else {
      val response = Mail.using(configuration)
        .to(to)
        .subject(subject)
        .text(filledPattern)
        .build()
        .send()
      MailerResponse(response.isOk(), response.responseCode(), response.responseMessage())
    }
  }

  override def sendPasswordRecoveryToken(to: String, login: String, code: String): MailerResponse = {

    val subject = config.getString("mailer.recoverpassword.subject")
    val mailPattern = config.getString("mailer.recoverpassword.pattern")

    val filledPattern = mailPattern
      .replace("%account.login%", login)
      .replace("%account.passwordRecoveryCode%", code)

    if (isLogLettersToConsole)
      Logger.debug(filledPattern)

    if (isDummyLetters) {
      MailerResponse(true, 200, "")
    } else {
      val response = Mail.using(configuration)
        .to(to)
        .subject(subject)
        .text(filledPattern)
        .build()
        .send()
      MailerResponse(response.isOk(), response.responseCode(), response.responseMessage())
    }
  }

  override def sendDebugInfo(subject: String, content: String): MailerResponse = {
    if (isLogDebugLettersToConsole)
      Logger.debug(content)

    if (isDummyDebugLetters) {
      MailerResponse(true, 200, "")
    } else {
      val response = Mail.using(configuration)
        .to(debugInfoTo)
        .subject(subject)
        .text(content)
        .build()
        .send()
      MailerResponse(response.isOk(), response.responseCode(), response.responseMessage())
    }
  }


}
