package com.magicube.eventflows.Spring

import org.springframework.boot.{CommandLineRunner, SpringApplication}

import scala.collection.mutable.Queue
import scala.reflect.ClassTag

abstract class SpringEntrance[T <: CommandLineRunner](runner: Class[T], cls: Class[_]*) {
  private val primarySources = {
    val container = Queue[Class[_]](runner)
    for (it <- cls) {
      container.enqueue(it)
    }
    container
  }
  protected val ctx = SpringApplication.run(primarySources.toArray[Class[_]], Array[String]())

  protected def getService[R: ClassTag](clasz: Class[R]) = ctx.getBean(clasz)
}


