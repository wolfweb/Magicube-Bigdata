package com.magicube.eventflows.flinkkafka

import java.nio.charset.StandardCharsets

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema
import org.apache.kafka.clients.consumer.ConsumerRecord

class JsonDeserializationSchema() extends KafkaDeserializationSchema[ObjectNode] {
  private val mapper = new ObjectMapper

  override def isEndOfStream(t: ObjectNode): Boolean = false

  override def deserialize(record: ConsumerRecord[Array[Byte], Array[Byte]]): ObjectNode = {
    val node = mapper.createObjectNode
    val messageKey = record.key()
    val topic = record.topic()
    val offset = record.offset()
    val partition = record.partition()

    val value = new String(record.value(), StandardCharsets.UTF_8)

    if (messageKey != null)
      node.put("key", messageKey)

    node.put("value", value.toString)
      .put("offset", offset)
      .put("topic", topic)
      .put("partition", partition)
  }

  override def getProducedType: TypeInformation[ObjectNode] = TypeExtractor.getForClass(classOf[ObjectNode])
}
