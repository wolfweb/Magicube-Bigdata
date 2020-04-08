package com.magicube.eventflows.web

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class WebApplication

object WebApplication{
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[WebApplication] , args:_ *)
  }
}
