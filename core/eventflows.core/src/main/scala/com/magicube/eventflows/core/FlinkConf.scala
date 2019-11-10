package com.magicube.eventflows.core

abstract class FlinkConf() {
  val interval: Long = 1500
  val parallelCount: Int = -1
  val retry:Int = 3
  val retryDelay = 1000
}
