package cl.reactivecloud

import cl.reactivecloud.utils.DistributedConfig
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.curator.utils.ZKPaths


/**
  * Created by gaston on 11/18/16.
  */

object ZookeeperLearning extends App with DistributedConfig {

  implicit val client = connectZookeeper()
  val mapper = new ObjectMapper()

  if (client.checkExists().forPath("/node1") == null) {
    println("creando node1 en /rc1/node1")
    client.create().forPath("/node1", "node 1 data".getBytes())
  }

  println("obteniendo data de /rc1/node1")
  println(new String(client.getData.forPath("/node1")))


  if (client.checkExists().forPath("/node1/subnode1") == null) {
    client.create().forPath(ZKPaths.makePath("/node1", "/subnode1"), mapper.writeValueAsBytes("no esun int"))
  }
  println(getOptionalSetting("/node1/subnode1").asOptionalInt.getOrElse(100))
  println(getOptionalSetting("/node1/subnode1").asOptionalString.getOrElse("S/I"))

  val state = client.getState
  println("Estado del cliente", state)

  System.exit(1)
}
