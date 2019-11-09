package com.magicube.eventflows.Es

import java.io.IOException

import com.google.gson.GsonBuilder
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.client.{JestClient, JestClientFactory, JestResult}
import io.searchbox.cluster.{Health, NodesInfo, NodesStats}
import io.searchbox.core._
import io.searchbox.indices._
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

case class ElasticsearchProvider[T <: ElasticModel : ClassTag](conf: ElasticConf)(implicit clasz: Class[T]) extends Serializable {
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

  def deleteIndex(): JestResult = {
    val entityType = clasz.getSimpleName
    val deleteIndex = new DeleteIndex.Builder(entityType).build()
    var result: JestResult = null
    try {
      result = client.execute(deleteIndex)
      println(s"deleteIndex----------${result.getJsonString}")
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
    val entityType = clasz.getSimpleName
    val closeIndex = new CloseIndex.Builder(entityType).build
    var result: JestResult = null
    try {
      result = client.execute(closeIndex)
      println(s"closeIndex----------${result.getJsonString}")
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
      println(s"optimizeIndex----------${result.getJsonString}")
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
      println(s"flushIndex----------${result.getJsonString}")
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

  def updateDocument(doc: String, id: String): JestResult = {
    val entityType = clasz.getSimpleName
    val update = new Update.Builder(doc).index(conf.index).`type`(entityType).id(id).build()
    var result: JestResult = null
    try {
      result = client.execute(update)
      println(s"updateDocument----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def deleteDocument(id: String): JestResult = {
    val entityType = clasz.getSimpleName
    val delete = new Delete.Builder(id).index(conf.index).`type`(`entityType`).build
    var result: JestResult = null
    try {
      result = client.execute(delete)
      println(s"deleteDocument----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def deleteDocumentByQuery(params: String): JestResult = {
    val entityType = clasz.getSimpleName
    var query = new DeleteByQuery.Builder(params).addIndex(conf.index).addType(entityType).build()
    var result: JestResult = null
    try {
      result = client.execute(query)
      println(s"deleteDocument----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
    result
  }

  def getDocument[T](id: String): T = {
    val entityType = clasz.getSimpleName
    val get = new Get.Builder(conf.index, id).`type`(entityType).build()
    var res: T = null.asInstanceOf[T]
    try {
      val result = client.execute(get)
      println(s"deleteDocument----------${result.getJsonString}")
      res = result.getSourceAsObject(clasz).asInstanceOf[T]
    } catch {
      case e: IOException => e.printStackTrace
    }
    res
  }

  def queryAll[T](): List[T] = {
    val searchSourceBuilder = new SearchSourceBuilder
    searchSourceBuilder.query(QueryBuilders.matchAllQuery())

    val search = new Search.Builder(searchSourceBuilder.toString).addIndex(conf.index).build()
    var list: List[T] = null
    try {
      val result = client.execute(search)
      list = result.getHits(clasz).map(x => x.source.asInstanceOf[T]).toList
    } catch {
      case e: IOException => e.printStackTrace
    }
    list
  }

  def query[T](fields: Map[String, Any]): List[T] = {
    val searchSourceBuilder = new SearchSourceBuilder
    var query = QueryBuilders.boolQuery()

    for (field <- fields) {
      query = query.filter(QueryBuilders.termQuery(field._1, field._2))
    }
    searchSourceBuilder.query(query)
    val search = new Search.Builder(searchSourceBuilder.toString).addIndex(conf.index).build()
    var list: List[T] = null
    try {
      val result = client.execute(search)
      if(result.isSucceeded) {
        list = result.getHits(clasz).map(x => x.source.asInstanceOf[T]).toList
      }
    } catch {
      case e: IOException => e.printStackTrace
    }
    list
  }


  def createIndex[T](t: T): Unit = {
    val entityType = clasz.getSimpleName
    val index = new Index.Builder(t).index(conf.index).`type`(entityType).build()
    var result: JestResult = null
    try {
      result = client.execute(index)
      println(s"createIndex----------${result.getJsonString}")
    } catch {
      case e: IOException => e.printStackTrace
    }
  }
}
