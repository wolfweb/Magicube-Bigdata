package com.magicube.eventflows.flinkkafka

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream

trait EventParseSinkHandler[T <: IKafkaEvent] extends Serializable {
  val identity: String

  val kafkaConf: KafkaConf

  def parse(x: T): T

  def invoke(stream: DataStream[T])
}

class EventParseProvider[T <: IKafkaEvent](handlers: List[EventParseSinkHandler[T]])(implicit clasz: Class[T]) extends Serializable {
  implicit val ttype = TypeInformation.of(clasz)

  def emit(stream: DataStream[T]): Unit = {
    for (handler <- handlers) {
      handler.invoke(stream.filter((x: IKafkaEvent) => getTopics(handler).contains(x.Topic)).map(x => handler.parse(x)))
    }
  }

  private def getTopics(handler: EventParseSinkHandler[T]): Array[String] = {
    handler.kafkaConf.topics.filter(x => x.handler.equals(handler.identity)).map(x => x.topic).toArray
  }
}
