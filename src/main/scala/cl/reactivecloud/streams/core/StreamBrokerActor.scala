package cl.reactivecloud.streams.core

import akka.actor.{Actor, ActorLogging, Props}
import cl.reactivecloud.commons._
import cl.reactivecloud.storage.{StreamStorage, SessionStorage, TokenValidator}

/**
  * Created by papelacho on 2016-10-24.
  */
object StreamBrokerActor {
  /**
    *
    * @param sessionStorage
    * @param tokenValidator
    * @param instanceStorage
    * @return
    */
  def props()(implicit sessionStorage: SessionStorage, tokenValidator: TokenValidator, instanceStorage: StreamStorage): Props = Props(new StreamBrokerActor())
}

class StreamBrokerActor(implicit val sessionStorage: SessionStorage, implicit val tokenValidator: TokenValidator, implicit val instanceStorage: StreamStorage) extends Actor with ActorLogging {

  def receive = {
    case Message(x) => x match {

      // Si Connect, obtiene sesion e instancia
      case connect: Connect =>
        //    Valida Login.token o Response(error)
        // Valida el id de session por que debe haber pasado por un StreamConnector, el cual realmente crea
        sessionStorage.getSession(connect.sessionId) match {
          case Some(session) =>

            // crea nombre para instancia, puede que sean varias sesiones conectandose a la misma
            val instanceId = instanceStorage.createName(session, connect)

            // crea o obtiene instancia
            val instance = instanceStorage.getOrUpdate(instanceId, context.system.actorOf(StreamActor.props(instanceId), instanceId))

            // actualiza sesion con instancia
            session.instanceId = instanceId
            // TODO guardar session

            // agrega sesion a instancia pero que notifique al original
            instance.tell(Message(ConnectSession(session.id)), sender)

          case _ => sender ! Response(Left("Internal Connection Failed: No Session Available"))
        }
      case _ => sender ! Response(Left("Internal Connection Failed: Wrong Message"))
    }
    case _ => sender ! Response(Left("Internal Connection Failed:Wrong Interaction"))
  }

}
