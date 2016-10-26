package cl.reactivecloud.storage

import java.util.UUID

import akka.actor.ActorRef
import cl.reactivecloud.commons.{Connect, Login}

import scala.collection.mutable

/**
  * Created by papelacho on 2016-10-25.
  */
trait StreamStorage {

  def get(streamId: String): Option[ActorRef]

  def getOrUpdate(streamId: String, f: => ActorRef): ActorRef

  def createName(session: Session, connect: Connect) = {
    val cid = connect.cid.getOrElse(UUID.randomUUID().toString)
    s"${session.user.name}-${cid}"
  }
}

class MapStreamStorage extends StreamStorage {
  val namedInstance = mutable.Map[String, ActorRef]()

  override def get(streamId: String): Option[ActorRef] = namedInstance.get(streamId)

  override def getOrUpdate(streamId: String, f: => ActorRef): ActorRef = namedInstance.getOrElseUpdate(streamId, f)


}