package cl.reactivecloud.commons

/**
  * Created by papelacho on 2016-10-10.
  */

class Base

class Event extends Base

case class Login(token: String)

case class User(name:String)

case class Subscribe(queues: Seq[String])

case class Unsubscribe(queues: Seq[String])

case class Subscription(queues: Seq[String])

case class Poll(quantity: Int)

case object Ping

case object Pong

case class Connect(sessionId: Option[String] = Option.empty, cid: Option[String] = Option.empty)

case class ConnectSession(sessionId: String)

case object Disconnect

case class DisconnectSession(sessionId: String)

case class Unbound(sid: String)

case class Response[T](value: Either[String, T])

case class Message[T](value: T)
