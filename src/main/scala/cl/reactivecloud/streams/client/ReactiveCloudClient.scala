package cl.reactivecloud.streams.client

import cl.reactivecloud.commons.{Base, Event, Message, Response}

/**
  * Created by papelacho on 2016-10-24.
  */

trait ReactiveCloudClient {
  /**
    *   Inicia conexion con connector-*
    */
  def connect() : Response[Boolean]

  /**
    * Envia Message[Login]
    * @param token
    * @param sid
    * @param cid
    * @return
    */
  def login(token:String, sid:String = null, cid:String = null) : Response[String]

  /**
    *   Envia Message[Subscribe]
    *   Actualiza lista local de subscriptions en respuesta exitosa
    * @param queues
    * @return
    */
  def subscribe(queues: List[Base]) : Response[List[Base]]

  /**
    * Envia Message[Poll]
    * @param qty
    * @return
    */
  def poll(qty:Int) : Response[List[Event]]


  /**
    *   Envia Message[Unsubscribe]
    *   Actualiza lista local de subscriptions en respuesta exitosa
    * @param queues
    * @return
    */
  def unsubscribe(queues: Base*) : Message[List[Base]]

  /**
    *   Cierra conexion con connector-*
    * @return
    */
  def disconnect() : Response[Boolean]

}