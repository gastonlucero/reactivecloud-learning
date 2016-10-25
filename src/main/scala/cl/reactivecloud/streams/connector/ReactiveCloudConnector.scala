package cl.reactivecloud.streams.connector

import cl.reactivecloud.commons.{Message, Response}

/**
  * Created by papelacho on 2016-10-24.
  */
trait ReactiveCloudConnector {
  /**
    * Crea connectionActor seteando metodo onResponse
    *
    * @return
    */
  def onConnect(): Response[Boolean]

  /**
    * Forward a connectionActor
    *
    * @param message
    * @return
    */
  def onMessage(message: Message[Any]): Response[Any]

  /**
    * Forward a client-*
    *
    * @param response
    * @return
    */
  def onResponse(response: Response[Any]): Unit

  /**
    * Envia Message[Disconnect] a connectionActor
    * Mata connectionActor
    * Cierra conexion
    *
    * @return
    */
  def onDisconnect(): Response[Any]

}

