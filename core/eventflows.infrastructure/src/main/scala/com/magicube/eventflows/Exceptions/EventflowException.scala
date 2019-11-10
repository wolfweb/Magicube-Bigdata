package com.magicube.eventflows.Exceptions

case class EventflowException(msg: String, code: Int = 0)
  extends Exception(msg) {

}
