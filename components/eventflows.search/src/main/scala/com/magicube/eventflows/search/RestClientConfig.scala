package com.magicube.eventflows.search

import org.apache.http.HttpHost
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration

@Configuration
case class RestClientConfig(conf: ElasticConfig) extends AbstractElasticsearchConfiguration {
  override def elasticsearchClient(): RestHighLevelClient = new RestHighLevelClient(RestClient.builder(conf.urls.map(x => new HttpHost(x.host, x.port, x.schema)): _*))
}

