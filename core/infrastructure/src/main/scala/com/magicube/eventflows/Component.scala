package com.magicube.eventflows

import com.typesafe.config.{Config, ConfigFactory}

trait Component {
  protected var conf: Config = ConfigFactory.empty()

  def name: String = this.getClass.getName

  def initConfig(): Unit

  def setConfig(config: Config): Unit = {
    conf = config
    initConfig()
  }
}

