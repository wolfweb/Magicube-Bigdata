package com.magicube.eventflows.search

import org.springframework.data.annotation.Id

trait ElasticModel[TKey] extends Serializable {
  @Id
  var id: TKey
}
