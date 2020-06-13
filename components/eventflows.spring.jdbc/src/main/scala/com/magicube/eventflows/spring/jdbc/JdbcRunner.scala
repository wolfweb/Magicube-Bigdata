package com.magicube.eventflows.spring.jdbc

import com.magicube.eventflows.Repository.IEntityBase
import com.magicube.eventflows.Spring.{SpringEntrance, SpringRunner}
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication

case class JdbcEntrance() extends SpringEntrance[JdbcRunner](classOf[JdbcRunner]) {
  def repository[T <: IEntityBase[TKey], TKey] = getService[Repository[T, TKey]](classOf[Repository[T, TKey]])
}

@SpringBootApplication
case class JdbcRunner() extends CommandLineRunner {
  override def run(args: String*): Unit = {
    println("component jdbc initialize!")
  }
}
