package com.magicube.eventflows.flinkkafka

trait IKafkaEvent {
  var Topic: String
  var Partition: Int
  var Offset: Long
  var Timestamp: Long
}
