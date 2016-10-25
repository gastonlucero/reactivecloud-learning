package cl.reactivecloud.session

import java.util.UUID

import akka.actor.ActorRef
import cl.reactivecloud.commons.Base

/**
  * Created by papelacho on 2016-10-24.
  */
class Session(val id:String = UUID.randomUUID().toString) {
  var subscriptions = Seq[Base]()
  var connector = ActorRef.noSender
  var core = ActorRef.noSender

  override def toString: String = s"Session($id,$connector,$core,$subscriptions)"
}
