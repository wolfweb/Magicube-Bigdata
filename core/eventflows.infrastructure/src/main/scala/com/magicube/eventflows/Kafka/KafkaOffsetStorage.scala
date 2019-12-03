package com.magicube.eventflows.Kafka

import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition

abstract class KafkaOffsetStorage {
  def storeTopicWithOffset(): Unit

  def getTopicWithOffset(): Option[java.util.Map[KafkaTopicPartition, java.lang.Long]]
}
