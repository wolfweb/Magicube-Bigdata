package com.magicube.eventflows.flinkkafka

import java.net.URLDecoder

class KafkaEvent
(
  topic: String,
  partition: Int,
  offset: Long,
  dict: Map[String, String]
) extends IKafkaEvent {
  var Topic = topic
  var Offset = offset
  var Partition = partition
  var Timestamp: Long = 0

  val Dict = dict

  override def toString: String = {
    dict.map(_.productIterator.mkString("=")).mkString("&")
  }
}

object KafkaEvent {
  private def splitQuery(data: String) = {
    var query_pairs = Map[String, String]()
    val pairs = data.split("&")
    for (pair: String <- pairs) {
      val idx = pair.indexOf("=")
      query_pairs += (URLDecoder.decode(pair.substring(0, idx), "UTF-8") -> URLDecoder.decode(pair.substring(idx + 1), "UTF-8"))
    }
    query_pairs
  }

  def fromString(topic: String, partition: Int, offset: Long, data: String): KafkaEvent = {
    new KafkaEvent(topic, partition, offset, splitQuery(data))
  }
}
