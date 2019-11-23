package com.magicube.eventflows.flinkkafka

trait KafkaEventbusHandler[T <: IKafkaEvent] {
  def Emit(value: T): Unit
}
