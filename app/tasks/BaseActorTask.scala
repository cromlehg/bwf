package tasks

import akka.actor.ActorSystem
import com.typesafe.config.Config
import javax.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class BaseActorTask @Inject()(actorSystem: ActorSystem, config: Config)(implicit executionContext: ExecutionContext) {

  actorSystem.scheduler.schedule(initialDelay = 1.minutes, interval = 100000000.milliseconds) {
  }

}