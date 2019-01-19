package security

import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}

import scala.collection.immutable.Map
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BaseDynamicResourceHandler extends DynamicResourceHandler {

  override def isAllowed[A](name: String, meta: Option[Any], handler: DeadboltHandler, request: AuthenticatedRequest[A]): Future[Boolean] =
    BaseDynamicResourceHandler.handlers(name).isAllowed(
      name,
      meta,
      handler,
      request)

  // todo implement this when demonstrating permissions
  override def checkPermission[A](
                                   permissionValue: String,
                                   meta: Option[Any] = None,
                                   deadboltHandler: DeadboltHandler,
                                   request: AuthenticatedRequest[A]): Future[Boolean] =
    Future.successful(false)

}

object BaseDynamicResourceHandler {

  val handlers: Map[String, DynamicResourceHandler] =
    Map(
      "pureLuck" -> new DynamicResourceHandler() {

        override def isAllowed[A](name: String, meta: Option[Any], deadboltHandler: DeadboltHandler, request: AuthenticatedRequest[A]): Future[Boolean] =
          Future.successful(System.currentTimeMillis() % 2 == 0)

        override def checkPermission[A](
                                         permissionValue: String,
                                         meta: Option[Any] = None,
                                         deadboltHandler: DeadboltHandler,
                                         request: AuthenticatedRequest[A]): Future[Boolean] =
          Future.successful(false)

      })

}