package security

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}
import javax.inject.{Inject, Singleton}
import models.dao.RoleDAO

@Singleton
class BaseHandlerCache @Inject()(authSupport: AuthSupport, roleDAO: RoleDAO) extends HandlerCache {

	val defaultHandler: DeadboltHandler = new BaseHandler(authSupport, roleDAO)()

	// HandlerKeys is an user-defined object, containing instances of a case class that extends HandlerKey
	val handlers: Map[Any, DeadboltHandler] = Map(
		HandlerKeys.defaultHandler -> defaultHandler,
		HandlerKeys.altHandler -> new BaseHandler(authSupport, roleDAO)(Some(BaseAlternativeDynamicResourceHandler)),
		HandlerKeys.userlessHandler -> new BaseAccountlessHandler(roleDAO),
		HandlerKeys.jsonHandler -> new JSONBasedHandler(authSupport, roleDAO))

	// Get the default handler.
	override def apply(): DeadboltHandler = defaultHandler

	// Get a named handler
	override def apply(handlerKey: HandlerKey): DeadboltHandler = handlers(handlerKey)
}
