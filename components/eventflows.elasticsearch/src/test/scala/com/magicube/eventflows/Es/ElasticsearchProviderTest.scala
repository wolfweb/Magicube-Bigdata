package scala.com.magicube.eventflows.Es

import java.util.UUID

import com.magicube.eventflows.Es.{ElasticFunction, ElasticModel}
import org.junit.Test

case class Foo(override var id: String , var name: String, var age: Int) extends ElasticModel[String]

class ElasticsearchProviderTest {
  private val url = "http://localhost:9200"
  private val index = "elastic-debug"

  implicit val clasz = classOf[Foo]

  @Test
  def func_elastic_test(): Unit = {
    val elastic = ElasticFunction[Foo,String](index, Array[String](url))

    var res = elastic.elastic.indicesExists()
    if(res.isSucceeded){
      res  =  elastic.elastic.deleteIndex()
      assert(res.isSucceeded)
    }

    val foo = Foo("1",UUID.randomUUID.toString, Math.random().toInt)
    elastic.create(foo)

    var allResult = elastic.elastic.queryAll()
    assert(allResult.size ==1)

    val queryFoo = elastic.elastic.get(Map[String, String]("id" -> foo.id))
    assert(queryFoo != null)
    queryFoo.name = "wolfweb"
    elastic.elastic.updateDocument(queryFoo)

    allResult = elastic.elastic.queryAll()
    assert(allResult.size ==1)

    var queryResult = elastic.elastic.query(Map[String, String]("name" -> "wolfweb"))
    assert(queryResult!=null && queryResult(0).name == "wolfweb")

    elastic.elastic.deleteDocument(queryResult(0))

    queryResult = elastic.elastic.query(Map[String, String]("id" -> foo.id))
    assert(queryResult == null || queryResult.size == 0)
  }

  @Test
  def func_elastic_create_with_multi_index: Unit = {
    val count = 10
    val elasticQuery = ElasticFunction[Foo,String](s"$index*", Array[String](url))

    var all = elasticQuery.elastic.queryAll()

    val elastic1 = ElasticFunction[Foo,String](s"$index-1", Array[String](url))
    val elastic2 = ElasticFunction[Foo,String](s"$index-2", Array[String](url))

    if (all.size > 0) {
      for (it <- all) {
        elasticQuery.elastic.deleteDocument(it)
      }

      all = elasticQuery.elastic.queryAll()
      assert(all.size == 0)
    }

    for (i <- 1 to count) {
      val foo = Foo(i.toString,s"${UUID.randomUUID.toString}_$i", i)
      if (i % 2 == 0) {
        elastic2.create(foo)
      } else {
        elastic1.create(foo)
      }
    }

    all = elasticQuery.elastic.queryAll()
    assert(all.size == count)

    for (i <- 1 to count) {
      val it = elasticQuery.elastic.get(Map("age" -> i))
      it.name = s"wolfweb_$i"
      elasticQuery.elastic.updateDocument(it)
    }

    for (i <- 1 to count) {
      val it = elasticQuery.elastic.get(Map("age" -> i))
      assert(it.name.startsWith("wolfweb"))
    }

    for (i <- 1 to count) {
      val it = elasticQuery.elastic.get(Map("age" -> i))
      elasticQuery.elastic.deleteDocument(it)
    }

    all = elasticQuery.elastic.queryAll()
    println(all.size)
    assert(all.size == 0)
  }
}
