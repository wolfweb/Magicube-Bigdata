package com.magicube.eventflows.flinkkafka

import java.util.Properties

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09

class Kafka09Service[T <: IKafkaEvent](broker: String, group: String, topics: java.util.List[String]) {
  private val _group = group
  private val _broker = broker
  private val _topics = topics
  private var popularSpots: DataStream[T] = _

  def start(transformHandlers: List[TransformHandler[T]]): Unit = {
    for (handler: TransformHandler[T] <- transformHandlers) {
      handler.transform(popularSpots)
    }
  }

  def buildStream(env: StreamExecutionEnvironment, dataFormatHandler: KafkaDataFormat[T], maxOutOfOrderness: Int)(implicit clasz: Class[T]): Kafka09Service[T] = {
    implicit val ttype = TypeInformation.of(clasz)

    val kafkaProperties = new Properties()
    kafkaProperties.setProperty("bootstrap.servers", _broker)
    kafkaProperties.setProperty("enable.auto.commit", "true")
    kafkaProperties.setProperty("group.id", _group)

    val kafkaConsumer = new FlinkKafkaConsumer09[ObjectNode](_topics, new JsonDeserializationSchema, kafkaProperties)

    kafkaConsumer.setStartFromGroupOffsets()
    //kafkaConsumer.setStartFromEarliest()

    val stream = env.addSource(kafkaConsumer)
    popularSpots = stream.map(dataFormatHandler).assignTimestampsAndWatermarks(KafkaEventTimestampAndWatermarkHandler[T](maxOutOfOrderness))

    this
  }
}
