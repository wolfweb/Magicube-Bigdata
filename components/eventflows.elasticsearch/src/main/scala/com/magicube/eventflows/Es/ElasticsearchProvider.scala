package com.magicube.eventflows.Es

import java.io.IOException

import com.google.gson.GsonBuilder
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory, JestResult}
import io.searchbox.cluster.{Health, NodesInfo, NodesStats}
import io.searchbox.core._
import io.searchbox.indices._
import io.searchbox.indices.mapping.PutMapping
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

import scala.collection.JavaConversions._
import scala.reflect.ClassTag
import com.magicube.eventflows.Json.JSON._
import com.magicube.eventflows._
import org.json4s.DefaultFormats

case class ElasticsearchProvider[T <: ElasticModel[TKey] : ClassTag, TKey](conf: ElasticConf)(implicit clasz: Class[T]) extends Serializable {
  val client: JestClient = {
    val factory = new JestClientFactory
    factory.setHttpClientConfig(new HttpClientConfig
    .Builder(conf.urls.toList)
      .gson(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create())
      .multiThreaded(true)
      .readTimeout(10000)
      .build())
    factory.getObject
  }

  val entityType = clasz.getSimpleName

  def deleteIndex(): JestResult = {
    val deleteIndex = new DeleteIndex.Builder(conf.index).build()
    var result: JestResult = null
    try {
      result = client.execute(deleteIndex)
      println(s"delete----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def clearCache(): JestResult = {
    val closeIndex = new ClearCache.Builder().build
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"clearCache----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def closeIndex(): JestResult = {
    val closeIndex = new CloseIndex.Builder(entityType).build
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"close----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def optimizeIndex(): JestResult = {
    val closeIndex = new Optimize.Builder().build
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"optimize----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def flushIndex(): JestResult = {
    val closeIndex = new Flush.Builder().build
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"flush----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def indicesExists(): JestResult = {
    val closeIndex = new IndicesExists.Builder(conf.index).build()
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"indicesExists----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def nodesInfo(): JestResult = {
    val closeIndex = new NodesInfo.Builder().build()
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"nodesInfo----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def health(): JestResult = {
    val closeIndex = new Health.Builder().build()
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"health----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def nodesStats(): JestResult = {
    val closeIndex = new NodesStats.Builder().build()
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"nodesStats----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def updateDocument(t: T): JestResult = {
    val doc = Document(t)
    val update = new Update.Builder(doc).index(t.$index).`type`(entityType).id(t.id.toString).refresh(true).build()
    var result: JestResult = null
    try {
      result = client.execute(update)
      println(s"update----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def deleteDocument(t: T): JestResult = {
    val delete = new Delete.Builder(t.id.toString).index(t.$index).`type`(`entityType`).refresh(true).build
    var result: JestResult = null
    try {
      result = client.execute(delete)
      println(s"delete----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def deleteDocumentByQuery(params: String): JestResult = {
    val query = new DeleteByQuery.Builder(params).addIndex(conf.index).addType(entityType).build()
    var result: JestResult = null
    try {
      result = client.execute(query)
      println(s"delete----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def get(where: Map[String, Any]): T = {
    val res = query(where)
    if (res.nonEmpty)
      res.head
    else null.asInstanceOf[T]
  }

  def get(id: String, index: String): T = {
    val get = new Get.Builder(index, id).`type`(entityType).build()
    var res: T = null.asInstanceOf[T]
    try {
      val result = client.execute(get)
      println(s"delete----------${result.getJsonString}")
      res = result.getSourceAsObject(clasz)
    } catch {
      case e: IOException => e.printStackTrace
    }
    res
  }

  def get(id: String): T = {
    get(id, conf.index)
  }

  def queryAll(): List[T] = {
    val searchSourceBuilder = new SearchSourceBuilder
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())

    val search = new Search.Builder(searchSourceBuilder.toString).addIndex(conf.index).build()
    var list: List[T] = null
    try {
      val result = client.execute(search)
      list = result.getHits(clasz).map(x => {
        val it = x.source
        it.$index = x.index
        it
      }).toList
    } catch {
      case e: IOException => e.printStackTrace
    }
    list
  }

  def query(where: Map[String, Any]): List[T] = {
    val searchSourceBuilder = new SearchSourceBuilder
    var query = QueryBuilders.boolQuery()

    for (field <- where) {
      query = query.filter(QueryBuilders.termQuery(field._1, field._2))
    }
    searchSourceBuilder.query(query)
    val search = new Search.Builder(searchSourceBuilder.toString).addIndex(conf.index).build()
    var list: List[T] = null
    try {
      val result = client.execute(search)
      if (result.isSucceeded) {
        list = result.getHits(clasz).map(x => {
          val it = x.source
          it.$index = x.index
          it
        }).toList
      }
    } catch {
      case e: IOException => e.printStackTrace
    }
    list
  }

  def createDoc(t: T): Unit = {
    val index = new Index.Builder(t).index(conf.index).`type`(entityType).id(t.id.toString).refresh(true).build()
    var result: JestResult = null
    try {
      result = client.execute(index)
      println(s"create----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
  }

  def createIndex(index:String = ""):Unit={
    val action = new CreateIndex.Builder(if(index.isNullOrEmpty) conf.index else index).build()
    val result =  client.execute(action)
    println(s"create index----------${result.getJsonString}")
  }

  def updateIndexMapping(mapping: IndexMapping):Unit= {
    val entityType = clasz.getSimpleName
    val source = Map[String,IndexMapping](entityType->mapping)
    val putMapping = new PutMapping.Builder(conf.index,entityType, serialize(source, DefaultFormats))
    val result = client.execute(putMapping.build())
    println(s"update mapping----------${result.getJsonString}")
  }

  case class Document(doc: T)
}

case class IndexMapping
(
  properties: Map[String,Map[String,Any]]
)
