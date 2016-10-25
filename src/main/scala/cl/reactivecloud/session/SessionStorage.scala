package cl.reactivecloud.session

import java.util.UUID

import cl.reactivecloud.commons.Response

import scala.collection.mutable

/**
  * Created by papelacho on 2016-10-24.
  */
trait SessionStorage {

  def getSession(id: String): Option[Session]

  def createSession(): Session

  def getOrCreateSession(id: Option[String]): Session = id match {
    case x: Some[String] => getSession(x.get).getOrElse(createSession)
    case _ => createSession
  }

  def ifValidSession[T](sid: Option[String], code: (Session) => Response[T]): Response[T] = {
    sid match {
      case Some(id) => getSession(id) match {
        case Some(session) => code(session)
        case _ => Response(Left(new Exception("No Session Available 1")))
      }
      case _ => Response(Left(new Exception("No Session Available 2")))
    }
  }
}

class MapSessionStorage extends SessionStorage {

  val store = mutable.Map[String, Session]()

  def getSession(id: String) = store.get(id)

  def createSession() = {
    val session = new Session(UUID.randomUUID.toString)
    store.getOrElseUpdate(session.id, session)
    session
  }
}