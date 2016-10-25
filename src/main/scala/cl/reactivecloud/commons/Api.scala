package cl.reactivecloud.commons

/**
  * Created by papelacho on 2016-10-10.
  */

class Base

class Event extends Base

case class Login(token: String, sid: Option[String] = Option.empty, cid: Option[String] = Option.empty)

case class Subscribe(queues: Seq[Base])

case class Poll(quantity: Int)

case class Unsubscribe(queues: Seq[Base])

case object Ping

class Disconnect

case class Response[T](value: Either[Exception, T])

case class Message[T](value: T)
