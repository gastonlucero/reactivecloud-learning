package cl.reactivecloud.streams.connector

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import cl.reactivecloud.commons.{Login, Message, Response}
import cl.reactivecloud.session.{Session, SessionStorage, TokenValidator}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await

/**
  * Created by papelacho on 2016-10-25.
  */
object StreamConnector {
  /**
    *
    * @param sessionStorage
    * @param tokenValidator
    * @return
    */
  def props(broker: ActorRef, onResponse: (Response[Any]) => Unit)(implicit sessionStorage: SessionStorage, tokenValidator: TokenValidator): Props = Props(new StreamConnector(broker, onResponse))
}

class StreamConnector(broker: ActorRef, onResponse: (Response[Any]) => Unit)(implicit val sessionStorage: SessionStorage, implicit val tokenValidator: TokenValidator) extends Actor with ActorLogging {

  import scala.concurrent.duration._
  import context._

  implicit val timeout = Timeout(5 seconds)

  def receive = {
    case x: Message[Login] =>
      //        Valida Login.token o Response(error)
      tokenValidator.validate(x.value) match {

        case Some(user) =>
          val session = sessionStorage.getOrCreateSession(x.value.sid)
          // Si Session.connector entonces Response(error)
          if (session.connector != ActorRef.noSender) {
            sender ! Response(Left(new Exception("Already connected!")))

          }

          // Si !Session.connector entonces actualiza Session.connector y Response(Session.sid)
          else {
            // Guarda self en Session.connector
            session.connector = self
            if (session.core == ActorRef.noSender) {
              // Envia Message(Login.copy(sid = Session.id)) a streams-core.broker
              val newLogin = Message(x.value.copy(sid = Option(session.id)))
              val future = broker ? newLogin
              val result = Await.result(future, timeout.duration).asInstanceOf[Response[ActorRef]]
              result match {
                // y guarda ActorRef en Session.core
                case Response(Right(coreInstance)) => {
                  session.core = coreInstance
                  becomeLoggedIn(session)
                }
                // o falla
                case x => sender ! x
              }
            }
            // si ya estamos listos
            else {
              becomeLoggedIn(session)
            }
            // this.logged = true
          }
        case _ => sender ! Response(Left(new Exception("Login Failed")))
      }
    case _ => sender ! Response(Left(new Exception("Login Failed")))
  }

  var id: String = null

  def becomeLoggedIn(session: Session) = {
    watch(session.core)
    onResponse(Response(Right(session.id)))
    become(loggedIn(session.id))
  }

  def loggedIn(sid: String): Receive = {
    case Message(x) =>
      x match {
        case x: Login =>
          onResponse(Response(Left(new Exception("Already Loggedin"))))
        case x =>
          // Si this.logged entonces Session.core.tell(x) si no Response(error)
          sessionStorage.getSession(sid) match {
            case Some(session) => session.core ! Message(x)
            case _ => unbecomeLoggedIn()
          }
      }
    case x: Response[Any] =>

      sessionStorage.getSession(sid) match {
        case Some(session) => onResponse(x)
        case _ => unbecomeLoggedIn()
      }
  }

  def unbecomeLoggedIn() = {
    onResponse(Response(Left(new Exception("No session available"))))
    unbecome()
  }

}
