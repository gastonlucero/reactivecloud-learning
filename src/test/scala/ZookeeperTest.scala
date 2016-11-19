import cl.reactivecloud.ZookeeperLearning._
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by gaston on 11/19/16.
  */
@RunWith(classOf[JUnitRunner])
class ZookeeperTest extends FunSuite {

  implicit val client = connectZookeeper()

  implicit val clientAuth = connectZookeeper("test", "test")

  test("Zookeeper client") {
    assert(client.getState.equals(CuratorFrameworkState.STARTED))
  }

  test("Zookeeper client with digest") {
    assert(clientAuth.getACL != null)
  }

  test("Create path under rc ") {
    if (client.checkExists().forPath("/node1") == null) {
      println("creando node1 en /rc1/node1")
      client.create().forPath("/node1", "node 1 data".getBytes())
    }
    assert(getSetting("/node1").asString != null)
  }
}
