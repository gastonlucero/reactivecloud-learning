
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.utils.ZKPaths
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by gaston on 11/19/16.
  */
@RunWith(classOf[JUnitRunner])
class ZookeeperTest extends FunSuite {

  import
  implicit val client = connectZookeeper()

  val clientAuth = connectZookeeper("test", "test")

  test("Zookeeper client") {
    assert(client.getState.equals(CuratorFrameworkState.STARTED))
  }

  test("Zookeeper client with digest") {
    assert(clientAuth.getACL != null)
  }

  test("Create path under rc ") {
    if (client.checkExists().forPath("/node1") == null) {
      client.create().forPath("/node1", "node 1 data".getBytes())
    }
    assert(client.checkExists().forPath("/node1").getNumChildren == 0)
  }

  test("Create children") {
    client.create().forPath(ZKPaths.makePath("/node1", "/subnode1"), "subnode1".getBytes)
    assert(client.checkExists().forPath("/node1").getNumChildren == 1)
  }

  test("Get data to node node1"){
    assert(getProperty("/node1").asString != null)
  }

  test("Set data to node node1"){
    client.setData().forPath("/node1"," append data to node 1".getBytes())
    assert(getProperty("/node1").asString != null)
  }

  test("Delete node subnode1"){
    client.delete().forPath("/node1/subnode1")
    assert(client.checkExists().forPath("/node1/subnode1") == null)
  }

}
