package cl.reactivecloud.streams.core

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import cl.reactivecloud.commons._
import cl.reactivecloud.storage.SessionStorage

/**
  * Created by papelacho on 2016-10-24.
  */
object StreamActor {
  /**
    *
    * @param sessionStorage
    * @return
    */
  def props(instanceId: String)(implicit sessionStorage: SessionStorage): Props = Props(new StreamActor(instanceId))
}

class StreamActor(instanceId: String)(implicit val sessionStorage: SessionStorage) extends Actor with ActorLogging {

  import context._

  // guarda estado de conexion
  var sessionIds = Seq[String]()
  var actorRefToSessionId = Map[ActorRef, String]()

  // esto deberia ser driver de kafka
  var position = 0
  var subscriptions = Seq[String]()

  def connectSession(connect: ConnectSession, sender: ActorRef) = {
    sessionIds ++= Seq(connect.sessionId)
    actorRefToSessionId += (sender -> connect.sessionId)
  }

  def disconnectSession(disconnect: DisconnectSession, sender: ActorRef): Unit = {
    sessionIds = sessionIds diff Seq(disconnect.sessionId)
    actorRefToSessionId -= sender
  }

  def disconnectSession(subject: ActorRef): Unit = {
    disconnectSession(DisconnectSession(actorRefToSessionId(subject)), subject)
  }


  def addSubscriptions(subscribe: Subscribe) = {
    // agregar
    subscriptions ++= subscribe.queues
    // responder
    broadcast(Response(Right(subscriptions)))
  }

  def removeSubscriptions(unsubscribe: Unsubscribe) = {
    // quitar
    subscriptions = subscriptions diff unsubscribe.queues
    // responder
    broadcast(Response(Right(subscriptions)))
  }

  def broadcast[T](response: Response[T]) = {
    sessionIds.map(sessionId => sessionStorage.getSession(sessionId)).foreach {
      case Some(session) =>
        session.connector ! response
      case _ => log.error("Session does not exists")
    }
  }

  def poll(poll: Poll): Response[Seq[Int]] = {
    val last = position + poll.quantity
    position = last
    Response(Right(position.to(last)))
  }

  def receive = {
    case Message(x) =>
      x match {
        case Ping => sender ! Response(Right(Pong))
        case x: Subscribe => addSubscriptions(x)
        case x: Unsubscribe => removeSubscriptions(x)
        case x: ConnectSession =>
          connectSession(x, sender)
          sender ! Response(Right(instanceId))
          watch(sender)
        case x: DisconnectSession =>
          disconnectSession(x, sender)
          unwatch(sender)
        case x: Poll => sender ! poll(x)
        case _ => sender ! Response(Left("Unknown Message"))
      }
    case Terminated(subject) =>
      disconnectSession(subject)
      unwatch(subject)

    case x => log.error("por que? {}", x)
  }

}