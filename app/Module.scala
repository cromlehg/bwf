
import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.AbstractModule
import models.dao._
import models.dao.slick._
import play.api.{Configuration, Environment}
import security.BaseHandlerCache
import services.{MailGunMailer, Mailer}

class Module(
              environment: Environment,
              configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[HandlerCache]).to(classOf[BaseHandlerCache])

    bind(classOf[AccountDAO]).to(classOf[SlickAccountDAO])
    bind(classOf[AccountDAOCloseHook]).asEagerSingleton()

    bind(classOf[RoleDAO]).to(classOf[SlickRoleDAO])
    bind(classOf[RoleDAOCloseHook]).asEagerSingleton()

    bind(classOf[PermissionDAO]).to(classOf[SlickPermissionDAO])
    bind(classOf[PermissionDAOCloseHook]).asEagerSingleton()

    bind(classOf[TagDAO]).to(classOf[SlickTagDAO])
    bind(classOf[TagDAOCloseHook]).asEagerSingleton()

    bind(classOf[SessionDAO]).to(classOf[SlickSessionDAO])
    bind(classOf[SessionDAOCloseHook]).asEagerSingleton()

    bind(classOf[OptionDAO]).to(classOf[SlickOptionDAO])
    bind(classOf[OptionDAOCloseHook]).asEagerSingleton()

    bind(classOf[PostDAO]).to(classOf[SlickPostDAO])
    bind(classOf[PostDAOCloseHook]).asEagerSingleton()

    bind(classOf[MenuDAO]).to(classOf[SlickMenuDAO])
    bind(classOf[MenuDAOCloseHook]).asEagerSingleton()

		bind(classOf[CommentDAO]).to(classOf[SlickCommentDAO])
		bind(classOf[CommentDAOCloseHook]).asEagerSingleton()

		bind(classOf[Mailer]).to(classOf[MailGunMailer])
  }

}
