package com.magicube.eventflows.search

import com.magicube.eventflows.Spring.SpringEntrance
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication

case class ElasticEntrance(cls: Class[_]*) extends SpringEntrance[ElasticRunner](classOf[ElasticRunner], cls: _*) {
  def elasticProvider[T <: ElasticModel[TKey], TKey]: ElasticsearchProvider[T, TKey] = {
    getService[ElasticsearchProvider[T, TKey]](classOf[ElasticsearchProvider[T, TKey]])
  }
}

@SpringBootApplication
case class ElasticRunner() extends CommandLineRunner {
  override def run(args: String*): Unit = {
    println("component search initialize!")
  }
}
