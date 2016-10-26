package cl.reactivecloud.streams.connector

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import cl.reactivecloud.commons._
import cl.reactivecloud.storage.{SessionStorage, StreamStorage, TokenValidator}

/**
  * Created by papelacho on 2016-10-25.
  */
object StreamConnector {
  def props(broker: ActorRef, onResponse: (Response[Any]) => Unit)(implicit sessionStorage: SessionStorage, tokenValidator: TokenValidator, instanceStorage: StreamStorage): Props = Props(new StreamConnector(broker, onResponse))
}

class StreamConnector(broker: ActorRef, onResponse: (Response[Any]) => Unit)
                     (implicit val sessionStorage: SessionStorage, implicit val tokenValidator: TokenValidator, implicit val instanceStorage: StreamStorage)
  extends Actor with ActorLogging with Stash{

  import context._
  log.debug("actor created")

  def receive = {
    case Message(x) => x match {
      case login: Login =>

        // Valida Login.token o Response(error)
        tokenValidator.validate(login) match {

          // si hay usuario
          case Some(user) =>

            // responder true
            onResponse(Response(Right(true)))

            // ir a modo loggedIn
            unstashAll()
            become(loggedIn(user), discardOld = false)

          // si no error
          case _ => onResponse(Response(Left("Login Failed: Auth Failed")))
        }

      // error
      case _ => stash()
    }
    case Response(x) => onResponse(Response(x))
  }

  def loggedIn(user: User): Receive = {
    // mensajes desde cliente
    case Message(x) => x match {

      case connect: Connect =>

        // obtener o crear session
        val session = sessionStorage.getOrCreateSession(user, connect.sessionId)

        // validar si existe un connector para la sesion
        if (session.connector != null) {
          // no puede haber 2 conexiones por session
          onResponse(Response(Left("Connection Failed: Already Connected")))

        } else {
          // setear y avisar al broker que se esta creando una conexion
          session.connector = self
          // TODO guardar session

          broker ! Message(Connect(Option(session.id), connect.cid))

          // convertirse al connecting
          unstashAll()
          become(connecting(session.id))

        }

      case _ => stash()
    }
    case Response(x) => onResponse(Response(x))
    case Disconnect =>
      self ! Disconnect
      unstashAll()
      unbecome()
  }

  def connecting(sessionId:String):Receive = {
    case Response(x) => x match {
      // si la conexion es exitosa
      case Right(r) => r match {

        // si es instanceId
        case streamId: String => {
          // responder ok
          onResponse(Response(Right(sessionId)))

          // ir a modo connected
          unstashAll()
          become(connected(sessionId, streamId), discardOld = false)
        }
        case _ => onResponse(Response(Left("Connection Failed: Wrong Response")))
      }
      case x =>
        onResponse(Response(x))
        unstashAll()
        unbecome()
    }
    case Disconnect =>
      self ! Disconnect
      unstashAll()
      unbecome()
    case _ => stash()
  }

  def connected(sessionId:String, streamId:String):Receive = {
    case Message(x) => instanceStorage.get(streamId) match {
      case Some(stream) =>
        x match {
          case Disconnect =>
            self ! Disconnect
            stream ! Message(DisconnectSession(sessionId))
            unbecome()
          case _ => stream ! Message(x)
        }
      case _ =>
        onResponse(Response(Left("Not Connected")))
        self ! Disconnect
        unbecome()
    }
    case Response(x) => onResponse(Response(x))
  }


}
