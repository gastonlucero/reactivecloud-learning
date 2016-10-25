package cl.reactivecloud.streams.core

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import cl.reactivecloud.commons.{Login, Message, Response}
import cl.reactivecloud.session.{SessionStorage, TokenValidator}

import scala.collection.mutable

/**
  * Created by papelacho on 2016-10-24.
  */
object StreamBrokerActor {
  /**
    *
    * @param sessionStorage
    * @param tokenValidator
    * @return
    */
  def props()(implicit sessionStorage: SessionStorage, tokenValidator: TokenValidator): Props = Props(new StreamBrokerActor())
}

class StreamBrokerActor(implicit val sessionStorage: SessionStorage, implicit val tokenValidator: TokenValidator) extends Actor with ActorLogging {

  val namedInstance = mutable.Map[String, ActorRef]()

  /**
    * Si viene un login, entonces crea una sesion y un actor asociado
    *
    * @return
    */
  def receive = {
    case x: Message[Login] =>

      //    Valida Login.token o Response(error)
      tokenValidator.validate(x.value) match {

        case Some(user) =>
          // Valida el id de session por que debe haber pasado por un cliente, el cual realmente crea
          sender ! sessionStorage.ifValidSession(x.value.sid, session => {
            val cid = x.value.cid.getOrElse(UUID.randomUUID().toString)
            // Si s"${user}-${cid}" entonces Response(ActorRef) o Response(system.actorOf[])
            val instance = namedInstance.getOrElseUpdate(s"$user-$cid", context.system.actorOf(StreamInstanceActor.props(user, x.value)))
            session.core = instance
            Response(Right(instance))
          })
        case _ => sender ! Response(Left(new Exception("Login Failed")))
      }

  }

}
