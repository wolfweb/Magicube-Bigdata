package com.magicube.eventflows.flinkkafka

case class KafkaConf(broker: String, group: String, topics: List[KafkaTopicHandlerMapping])

case class KafkaTopicHandlerMapping(topic: String, handler: String)

