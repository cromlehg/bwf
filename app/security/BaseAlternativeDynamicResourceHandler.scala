package security

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}

import scala.concurrent.Future

object BaseAlternativeDynamicResourceHandler extends DynamicResourceHandler {

	import scala.concurrent.Future.{successful => future}

	override def isAllowed[A](
														 name: String,
														 meta: Option[Any],
														 handler: DeadboltHandler,
														 request: AuthenticatedRequest[A]): Future[Boolean] =
		future(false)

	override def checkPermission[A](
																	 permissionValue: String,
																	 meta: Option[Any],
																	 deadboltHandler: DeadboltHandler,
																	 request: AuthenticatedRequest[A]): Future[Boolean] =
		future(false)

}
