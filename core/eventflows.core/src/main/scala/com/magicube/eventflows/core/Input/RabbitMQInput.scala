package com.magicube.eventflows.core.Input

import java.util

import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core.{FlinkConf, InputRawData}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig

case class RabbitMQInputConf
(
  host: String,
  port: Int,
  queueName: String
) extends FlinkConf {}

class RabbitMQInput extends InputComponent {
  private var rabbitMqConf: RabbitMQInputConf = _

  override def initConfig(): Unit = {
    val path = "server.input.rabbitmq"
    if (conf.hasPath(path)) {
      rabbitMqConf = conf.as[RabbitMQInputConf](path)
      if (rabbitMqConf == null || rabbitMqConf.host == null || rabbitMqConf.host.isEmpty || rabbitMqConf.port <= 0)
        throw EventflowException("rabbitmq input require host, port .etc")
    }
  }

  override def prepare(): DataStream[InputRawData] = {
    val connectionConfig = new RMQConnectionConfig.Builder()
      .setHost(rabbitMqConf.host)
      .setPort(rabbitMqConf.port)
      .build()

    val stream = env.addSource(new RMQSource[String](connectionConfig, rabbitMqConf.queueName, new SimpleStringSchema))
    stream.map(x => InputRawData(x, new util.HashMap[String, Any]()))
  }
}
