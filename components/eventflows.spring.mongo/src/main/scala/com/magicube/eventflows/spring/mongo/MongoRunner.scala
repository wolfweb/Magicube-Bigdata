package com.magicube.eventflows.spring.mongo

import com.magicube.eventflows.Repository.IEntityBase
import com.magicube.eventflows.Spring.SpringEntrance
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication

case class MongoEntrance() extends SpringEntrance[MongoRunner](classOf[MongoRunner]) {
  def repository[T <: IEntityBase[TKey], TKey] = getService[Repository[T, TKey]](classOf[Repository[T, TKey]])
}

@SpringBootApplication
case class MongoRunner() extends CommandLineRunner {
  override def run(args: String*): Unit = {
    println("component mongo initialize!")
  }
}
