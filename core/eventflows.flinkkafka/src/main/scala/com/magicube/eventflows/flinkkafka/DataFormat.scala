package com.magicube.eventflows.flinkkafka

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.slf4j.LoggerFactory

abstract class DataFormat[T <: IKafkaEvent]() extends MapFunction[ObjectNode, T] {
  protected val logger = LoggerFactory.getLogger(getClass.getName)
  def eventsHandler: KafkaEventbusHandler[T] = new KafkaEventbusHandler[T] {
    override def Emit(value: T): Unit = {}
  }
}

case class KafkaDataFormat[T <: IKafkaEvent](func: (String, Int, Long, String) => T) extends DataFormat[T] {
  override def map(t: ObjectNode): T = {
    val topic = t.get("topic").asText
    val value = t.get("value").toString
    val offset = t.get("offset").asLong
    val partition = t.get("partition").asInt

    logger.debug(s"fetch data: topic ${topic} with offset ${offset} on ${partition}\nbody:${value}")

    func(topic, partition, offset, value)
  }
}
