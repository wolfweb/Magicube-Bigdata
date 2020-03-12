package com.magicube.eventflows.Kafka

import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition

abstract class KafkaOffsetStorage {
  def storeTopicWithOffset(group: String, topic: String, partition: Int, offset: Long): Unit

  def getTopicWithOffset(): Option[java.util.Map[KafkaTopicPartition, java.lang.Long]]
}
