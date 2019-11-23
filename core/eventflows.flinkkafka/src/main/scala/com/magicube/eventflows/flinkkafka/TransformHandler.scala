package com.magicube.eventflows.flinkkafka

import org.apache.flink.streaming.api.scala.DataStream

abstract class TransformHandler[T <: IKafkaEvent] extends Serializable {
  val eventParseHandler: EventParseProvider[T]

  def transform(stream: DataStream[T]): Unit = {
    eventParseHandler.emit(stream)
    invoke(stream)
  }

  def invoke(stream: DataStream[T]): Unit
}
