package com.magicube.eventflows.Spring

import org.springframework.boot.{CommandLineRunner, SpringApplication}

import scala.reflect.ClassTag

abstract class SpringEntrance[T <: CommandLineRunner](model: Class[T]) {
  protected val ctx = SpringApplication.run(model)

  protected def getService[R: ClassTag](clasz: Class[R]) = ctx.getBean(clasz)
}


