package com.magicube.eventflows.flinkkafka

import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.windowing.time.Time

case class KafkaEventTimestampAndWatermarkHandler[T <: IKafkaEvent](maxOutOfOrderness: Int)
  extends BoundedOutOfOrdernessTimestampExtractor[T](Time.minutes(maxOutOfOrderness)) {
  override def extractTimestamp(element: T): Long = element.Timestamp
}
