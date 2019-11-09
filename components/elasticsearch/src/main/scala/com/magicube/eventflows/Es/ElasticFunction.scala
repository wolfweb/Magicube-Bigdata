package com.magicube.eventflows.Es

import scala.reflect.ClassTag

object ElasticFunction {
  private var elasticCache = Map[String, Object]()

  def buildElastic[T <: ElasticModel : ClassTag](index: String, urls: Array[String])(implicit clasz: Class[T]) = {
    var elastic: Option[(String, Object)] = elasticCache.find(x => x._1 == index)
    if (elastic == None) {
      val provider = ElasticsearchProvider[T](ElasticConf(index, urls))
      elasticCache += index -> provider
      elastic = Some(index -> provider.asInstanceOf[Object])
    }
    elastic.get._2.asInstanceOf[ElasticsearchProvider[T]]
  }
}

case class ElasticFunction[T <: ElasticModel : ClassTag](index: String, urls: Array[String])(implicit clasz: Class[T]) extends Serializable {
  val elastic: ElasticsearchProvider[T] = ElasticFunction.buildElastic[T](index, urls)

  def create(value: T): Unit = {
    elastic.createIndex(value)
  }

  def getOrAdd(value: T, func: () => Map[String, Any]): T = {
    val query = func()
    val res = elastic.query(query)
    if (res == null || res.size == 0) {
      create(value)
      value
    } else {
      res(0)
    }
  }
}