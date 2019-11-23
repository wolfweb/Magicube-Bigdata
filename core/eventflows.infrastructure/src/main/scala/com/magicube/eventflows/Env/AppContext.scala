package com.magicube.eventflows.Env

class AppContext private(conf: BaseAppConfig) {
  val appConf = conf
  val version = conf.version
  val appName = conf.name
}

object AppContext {
  var ctx: AppContext = null

  def init[T <: BaseAppConfig](conf: T) = {
    if (ctx == null)
      ctx = new AppContext(conf)
  }
}