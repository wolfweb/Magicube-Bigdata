package com.magicube.eventflows.Spring

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
case class SpringRunner() extends CommandLineRunner {
  override def run(args: String*): Unit = {
    println("component jdbc initialize!")
  }
}
