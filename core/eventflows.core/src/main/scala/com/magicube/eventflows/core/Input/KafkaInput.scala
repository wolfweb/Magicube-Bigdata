package com.magicube.eventflows.core.Input

import java.util
import java.util.Properties

import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core.{FlinkConf, InputRawData}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, KafkaDeserializationSchema, FlinkKafkaConsumer09}
import org.apache.kafka.clients.consumer.ConsumerRecord
import scala.collection.JavaConverters._

case class KafkaInputConf
(
  broker: String,
  group: String,
  topics: List[String],
  properties: Map[String, String] = null
) extends FlinkConf

class KafkaInput extends InputComponent {
  val consumerPrefix = "Eventflow"
  protected var kafkaConf: KafkaInputConf = _

  override def initConfig(): Unit = {
    val path = "server.input.kafka"
    if (conf.hasPath(path)) {
      kafkaConf = conf.as[KafkaInputConf](path)
      if (kafkaConf == null || kafkaConf.broker == null || kafkaConf.broker.isEmpty || kafkaConf.topics == null || kafkaConf.topics.size == 0)
        throw EventflowException("kafka input require broker, topics, group .etc")
    }
  }

  override def prepare(): DataStream[InputRawData] = {
    val kafkaProperties = parseKafkaProperties()
    val kafkaConsumer = new FlinkKafkaConsumer[InputRawData](kafkaConf.topics.asJava, new KafkaInputDeserializationSchema, kafkaProperties)
    kafkaConsumer.setStartFromGroupOffsets()
    env.addSource(kafkaConsumer)
  }

  protected def parseKafkaProperties(): Properties = {
    val kafkaProperties = new Properties()
    kafkaProperties.setProperty("bootstrap.servers", kafkaConf.broker)
    kafkaProperties.setProperty("auto.commit.enable", "true")
    kafkaProperties.setProperty("group.id", "%s-%s".format(consumerPrefix, kafkaConf.group))

    if (kafkaConf.properties != null) {
      for (it <- kafkaConf.properties) {
        kafkaProperties.setProperty(it._1, it._2)
      }
    }

    kafkaProperties
  }
}

class KafkaInputDeserializationSchema() extends KafkaDeserializationSchema[InputRawData] {
  override def isEndOfStream(t: InputRawData): Boolean = false

  override def deserialize(record: ConsumerRecord[Array[Byte], Array[Byte]]): InputRawData = {
    val mapper = new ObjectMapper()
    val hashMap = new util.HashMap[String, Any]()
    hashMap.put("offset", record.offset())
    hashMap.put("topic", record.topic())
    hashMap.put("partition", record.partition())
    val messageKey = record.key()
    if (messageKey != null)
      hashMap.put("key", mapper.readValue(messageKey, classOf[String]))
    InputRawData(mapper.readValue(record.value(), classOf[String]), hashMap)
  }

  override def getProducedType: TypeInformation[InputRawData] = TypeExtractor.getForClass(classOf[InputRawData])
}

