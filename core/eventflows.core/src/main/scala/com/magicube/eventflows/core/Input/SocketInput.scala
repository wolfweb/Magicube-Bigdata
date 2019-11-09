package com.magicube.eventflows.core.Input

import java.util

import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core.{FlinkConf, InputRawData}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.streaming.api.scala._

case class SocketInputConf
(
  host: String,
  port: Int
) extends FlinkConf

class SocketInput extends InputComponent {
  private var socketConf: SocketInputConf = _

  override def initConfig(): Unit = {
    val path = "server.input.socket"
    if (conf.hasPath(path)) {
      socketConf = conf.as[SocketInputConf](path)
      if (socketConf == null || socketConf.host == null || socketConf.host.isEmpty || socketConf.port <= 0)
        throw EventflowException("socket input require host, port .etc")
    }
  }

  override def prepare(): DataStream[InputRawData] = {
    val stream = env.socketTextStream(socketConf.host, socketConf.port)
    stream.map(x => InputRawData(x, new util.HashMap[String, Any]()))
  }
}
