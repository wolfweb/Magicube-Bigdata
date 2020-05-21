package com.magicube.eventflows.Es

import io.searchbox.annotations.JestId

trait ElasticModel[TKey] {
  @JestId
  var id: TKey
  var $index: String = ""
}
