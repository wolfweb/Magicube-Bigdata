package com.magicube.eventflows.search

import org.junit.Test
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.annotations.Document
import com.magicube.eventflows.Spring.ComponentRegistryPostProcessor

@Document(indexName = "foo")
case class FooElasticModel
(
  var id: Long,
  var name: String,
  var thumbnail: String,
  var createAt: String,
  var age: Long
) extends ElasticModel[Long]

@Configuration
case class ElasticConfigComponent() extends ComponentRegistryPostProcessor[ElasticConfig] {
  override def BuildInstance: ElasticConfig = ElasticConfig("Foo", Array[ElasticHost](ElasticHost("localhost", 9200)))
}

class SpringElasticsearchTest {
  @Test
  def func_elasticsearch_func(): Unit = {
    val service = ElasticEntrance().elasticProvider[FooElasticModel,Long]
    assert(service != null)
    assert(service.elastic.indexExists(classOf[FooElasticModel]))
    val foo = service.rest.get("1",classOf[FooElasticModel])
    assert(foo!=null)
  }
}
