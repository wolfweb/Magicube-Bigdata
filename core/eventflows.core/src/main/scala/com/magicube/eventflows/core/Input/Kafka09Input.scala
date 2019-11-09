package com.magicube.eventflows.core.Input

import com.magicube.eventflows.core.InputRawData
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer09
import scala.collection.JavaConverters._
import org.apache.flink.api.scala._

class Kafka09Input extends KafkaInput{
  override def prepare(): DataStream[InputRawData] = {
    val kafkaProperties = parseKafkaProperties()
    val kafkaConsumer = new FlinkKafkaConsumer09[InputRawData](kafkaConf.topics.asJava, new KafkaInputDeserializationSchema, kafkaProperties)
    kafkaConsumer.setStartFromGroupOffsets()
    env.addSource(kafkaConsumer)
  }
}
