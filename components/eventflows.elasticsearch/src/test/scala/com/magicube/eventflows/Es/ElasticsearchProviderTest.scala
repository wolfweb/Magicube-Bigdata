package scala.com.magicube.eventflows.Es

import java.util.UUID

import com.magicube.eventflows.Es.{ElasticFunction, ElasticModel}
import org.junit.Test

case class Foo(var name: String, var age: Int) extends ElasticModel

class ElasticsearchProviderTest {
  private val url = "http://localhost:9200"
  private val index = "debug"

  implicit val clasz = classOf[Foo]

  @Test
  def func_elastic_test(): Unit = {
    val elastic = ElasticFunction(index, Array[String](url))

    var res = elastic.elastic.indicesExists()
    if(res.isSucceeded){
      res  =  elastic.elastic.deleteIndex()
      assert(res.isSucceeded)
    }

    val foo = Foo(UUID.randomUUID.toString, Math.random().toInt)
    elastic.create(foo)

    var allResult = elastic.elastic.queryAll()
    assert(allResult.size ==1)

    val queryFoo = elastic.elastic.get(Map[String, String]("_id" -> foo.$id))
    assert(queryFoo != null)
    queryFoo.name = "wolfweb"
    elastic.elastic.updateDocument(queryFoo)

    allResult = elastic.elastic.queryAll()
    assert(allResult.size ==1)

    var queryResult = elastic.elastic.query(Map[String, String]("name" -> "wolfweb"))
    assert(queryResult!=null && queryResult(0).name == "wolfweb")

    elastic.elastic.deleteDocument(queryResult(0))

    queryResult = elastic.elastic.query(Map[String, String]("_id" -> foo.$id))
    assert(queryResult == null || queryResult.size == 0)
  }
}
