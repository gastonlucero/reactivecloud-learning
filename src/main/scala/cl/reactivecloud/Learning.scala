package cl.reactivecloud

import java.util.concurrent.{ArrayBlockingQueue, TimeUnit}

import akka.actor.ActorSystem
import akka.pattern.AskSupport
import akka.util.Timeout
import cl.reactivecloud.commons._
import cl.reactivecloud.storage.{MapStreamStorage, MapSessionStorage, TokenValidator}
import cl.reactivecloud.streams.connector.StreamConnector
import cl.reactivecloud.streams.core.StreamBrokerActor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

/**
  * Created by papelacho on 2016-10-24.
  */
object Learning extends App with AskSupport {

  implicit val system = ActorSystem("learning")
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  implicit val sessionStorage = new MapSessionStorage
  implicit val tokenValidator = new TokenValidator
  implicit val instanceStorage = new MapStreamStorage

  val streamBroker = system.actorOf(StreamBrokerActor.props(), "streamBroker")

  // implementacion de un conector tipo Cola
  val queue = new ArrayBlockingQueue[Response[Any]](10)

  def receive(response: Response[Any]): Unit = {
    queue.put(response)
  }

  var streamConnector = system.actorOf(StreamConnector.props(streamBroker, receive))
  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  new Thread(new Runnable {
    def run(): Unit = {
      while (true)
        println(mapper.writeValueAsString(queue.take()))
    }
  }).start()

  Thread.sleep(500)

  streamConnector ! Message(Login("wenaleke"))
  streamConnector ! Message(Connect(Option("hola")))
  streamConnector ! Message(Subscribe(Seq("cola_1")))
  streamConnector ! Message(Poll(10))
  streamConnector ! Message(Subscribe(Seq("cola_2")))
  streamConnector ! Message(Poll(10))
  streamConnector ! Message(Subscribe(Seq("cola_3")))
  streamConnector ! Message(Poll(10))
  streamConnector ! Message(Unsubscribe(Seq("cola_1", "cola_2")))
  streamConnector ! Message(Poll(10))
  streamConnector ! Message(Disconnect)

  //  streamConnector ! Message(Login("wenaleke"))
  //  streamConnector ! Message(Login("nopaanahelmano"))
  //  streamConnector ! Message(Login("wenaleke"))
  //  streamConnector ! Message(Subscribe(Seq("cola_3")))
  //  streamConnector ! Message(Poll(10))

  Thread.sleep(1000)

  system.terminate()
  System.exit(0)
}
