package cl.reactivecloud.streams.core

import akka.actor.{Actor, ActorLogging, Props}
import cl.reactivecloud.commons._
import cl.reactivecloud.session.SessionStorage

/**
  * Created by papelacho on 2016-10-24.
  */
object StreamInstanceActor {
  /**
    * Create Props for an actor of this type.
    *
    * @param login
    * @return
    */
  def props(user: String, login: Login)(implicit sessionStorage: SessionStorage): Props = Props(new StreamInstanceActor(user, login))
}

class StreamInstanceActor(val user: String, val login: Login)(implicit val sessionStorage: SessionStorage) extends Actor with ActorLogging {

  //    Crea conector kafka usando Login.cid.getOrElse(Login.sid)
  // guarda estado de conexion
  var position = 0

  def receive = {
    case Message(x) => x match {
      case Ping =>
        log.info("ping from {}", sender())

      case subscribe: Subscribe =>
        //  Actualiza conector.subscriptions
        //  Actualiza Session.subscriptions
        sender ! sessionStorage.ifValidSession(login.sid, session => {
          session.subscriptions ++= subscribe.queues
          //  Broadcast Response[Session.subscriptions]
          Response(Right(session.subscriptions))
        })

      case poll: Poll =>
        //  Ejecuta connector.poll hasta llenar arreglo de salida
        sender ! sessionStorage.ifValidSession(login.sid, session => {
          val last = position + poll.quantity
          position = last
          Response(Right(position.to(last)))
        })

      case unsubscribe: Unsubscribe =>
        //  Actualiza conector.subscriptions
        //  Actualiza Session.subscriptions
        sender ! sessionStorage.ifValidSession(login.sid, session => {
          session.subscriptions = session.subscriptions diff unsubscribe.queues
          //  Broadcast Response[Session.subscriptions]
          Response(Right(session.subscriptions))
        })

      case x: Disconnect =>
      //  Cierra connector
    }
  }

}