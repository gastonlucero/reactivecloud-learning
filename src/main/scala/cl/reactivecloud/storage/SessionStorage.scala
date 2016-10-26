package cl.reactivecloud.storage

import java.util.UUID

import cl.reactivecloud.commons.{Response, User}

import scala.collection.mutable

/**
  * Created by papelacho on 2016-10-24.
  */
trait SessionStorage {

  def getSession(id: String): Option[Session]

  def getSession(id: Option[String]): Option[Session] = id match {
    case Some(x) => getSession(x)
    case _ => None
  }

  def createSession(user:User): Session

  def getOrCreateSession(user:User, id: Option[String]): Session = id match {
    case Some(x) => getSession(x).getOrElse(createSession(user))
    case _ => createSession(user)
  }

  def ifValidSession[T](sid: Option[String], code: (Session) => Response[T]): Response[T] = {
    sid match {
      case Some(id) => getSession(id) match {
        case Some(session) => code(session)
        case _ => Response(Left("No Session Available 1"))
      }
      case _ => Response(Left("No Session Available 2"))
    }
  }
}

class MapSessionStorage extends SessionStorage {

  val store = mutable.Map[String, Session]()

  def getSession(id: String) = store.get(id)

  def createSession(user:User) = {
    val session = new Session(user, UUID.randomUUID.toString)
    store.getOrElseUpdate(session.id, session)
  }
}