package com.magicube.eventflows.core

abstract class FlinkConf() {
  val interval: Long = 1500
  val parallelCount: Int = -1
}
