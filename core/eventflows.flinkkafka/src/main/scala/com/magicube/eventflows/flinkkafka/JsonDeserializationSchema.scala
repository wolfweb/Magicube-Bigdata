package com.magicube.eventflows.flinkkafka

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema

class JsonDeserializationSchema() extends KeyedDeserializationSchema[ObjectNode] {
  private val mapper = new ObjectMapper

  override def isEndOfStream(t: ObjectNode): Boolean = false

  override def deserialize(messageKey: Array[Byte], message: Array[Byte], topic: String, partition: Int, offset: Long): ObjectNode = {
    val node = mapper.createObjectNode
    if (messageKey != null)
      node.set("key", mapper.readValue(messageKey, classOf[JsonNode]))

    if (message != null)
      node.set("value", mapper.readValue(message, classOf[JsonNode]))

    node.put("offset", offset)
      .put("topic", topic)
      .put("partition", partition)
  }

  override def getProducedType: TypeInformation[ObjectNode] = TypeExtractor.getForClass(classOf[ObjectNode])
}
