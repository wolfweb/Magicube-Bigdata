package com.magicube.eventflows.core.Format

import com.magicube.eventflows.Component
import com.magicube.eventflows.core.InputRawData
import org.apache.flink.streaming.api.scala.DataStream

abstract class FormatComponent extends Component {
  val order: Int

  def formatData(stream: DataStream[InputRawData]): DataStream[InputRawData]
}

