package com.magicube.eventflows.core.Transform

import com.magicube.eventflows.Component
import com.magicube.eventflows.core.InputRawData
import org.apache.flink.streaming.api.scala._

abstract class TransformComponent extends Component {
  val order: Int

  def transformData(stream: DataStream[InputRawData]): DataStream[InputRawData]
}
