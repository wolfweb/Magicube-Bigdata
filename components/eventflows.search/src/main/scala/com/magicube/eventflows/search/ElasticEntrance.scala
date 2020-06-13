package com.magicube.eventflows.search

import com.magicube.eventflows.Spring.SpringEntrance
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication

case class ElasticEntrance() extends SpringEntrance[ElasticRunner](classOf[ElasticRunner]) {
  def elasticProvider[T <: ElasticModel[TKey], TKey] = getService[ElasticsearchProvider[T, TKey]](classOf[ElasticsearchProvider[T, TKey]])
}

@SpringBootApplication
case class ElasticRunner() extends CommandLineRunner {
  override def run(args: String*): Unit = {
    println("component search initialize!")
  }
}
