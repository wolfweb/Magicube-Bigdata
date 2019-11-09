package com.magicube.eventflows.core.Output

import com.magicube.eventflows.Component
import com.magicube.eventflows.core.InputRawData
import org.apache.flink.streaming.api.scala._

abstract class OutputComponent extends Component {
  def sinkData(stream: DataStream[InputRawData])
}
