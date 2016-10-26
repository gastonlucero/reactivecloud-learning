package cl.reactivecloud.storage

import java.util.UUID

import akka.actor.ActorRef
import cl.reactivecloud.commons.User

/**
  * Created by papelacho on 2016-10-24.
  */
class Session(val user:User, val id: String = UUID.randomUUID().toString) {
  var connector = ActorRef.noSender
  var instanceId : String = null

  override def toString: String = s"Session($id,$user,$connector,$instanceId)"
}
