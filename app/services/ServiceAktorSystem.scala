package services

object ServiceAktorSystem {

  implicit val actorSystem = akka.actor.ActorSystem.apply("WS-Actor-System-1")

}