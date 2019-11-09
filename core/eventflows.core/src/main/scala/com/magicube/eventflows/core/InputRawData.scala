package com.magicube.eventflows.core

import java.util

case class InputRawData
(
  rawStr: String,
  metadata: util.HashMap[String, Any],
  datas: util.HashMap[String, Any] = new util.HashMap[String, Any]()
)
