
import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.AbstractModule
import models.dao._
import models.dao.slick._
import play.api.{Configuration, Environment}
import security.BaseHandlerCache
import services._

class Module(environment: Environment,
						 configuration: Configuration) extends AbstractModule {

	override def configure(): Unit = {
		bind(classOf[HandlerCache]).to(classOf[BaseHandlerCache])

		bind(classOf[AccountDAO]).to(classOf[SlickAccountDAO])
		bind(classOf[AccountDAOCloseHook]).asEagerSingleton()

		bind(classOf[RoleDAO]).to(classOf[SlickRoleDAO])
		bind(classOf[RoleDAOCloseHook]).asEagerSingleton()

		bind(classOf[PermissionDAO]).to(classOf[SlickPermissionDAO])
		bind(classOf[PermissionDAOCloseHook]).asEagerSingleton()

		bind(classOf[SessionDAO]).to(classOf[SlickSessionDAO])
		bind(classOf[SessionDAOCloseHook]).asEagerSingleton()

		bind(classOf[InputSanitizer]).to(classOf[JSoupInputSanitizer])

		bind(classOf[OptionDAO]).to(classOf[SlickOptionDAO])
		bind(classOf[OptionDAOCloseHook]).asEagerSingleton()

		bind(classOf[DAOProvider]).to(classOf[SlickDAOProvider])

		bind(classOf[Mailer]).to(classOf[MailGunMailer])
	}

}
