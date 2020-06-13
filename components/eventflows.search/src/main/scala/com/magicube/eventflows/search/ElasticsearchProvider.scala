package com.magicube.eventflows.search

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.data.elasticsearch.core.{ElasticsearchOperations, ElasticsearchRestTemplate}
import org.springframework.stereotype.Service

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
case class ElasticsearchProvider[T <: ElasticModel[TKey], TKey](elastic: ElasticsearchOperations, rest: ElasticsearchRestTemplate) extends Serializable {

}
