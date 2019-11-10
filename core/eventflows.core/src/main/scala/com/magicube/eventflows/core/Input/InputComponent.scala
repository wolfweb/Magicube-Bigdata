package com.magicube.eventflows.core.Input

import com.magicube.eventflows.Component
import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core._
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.api.scala._

abstract class InputComponent() extends Component{
  protected var env: StreamExecutionEnvironment = buildExecutor()

  def buildExecutor(): StreamExecutionEnvironment = {
    checkConfig()
    val flinkConf = conf.asInstanceOf[FlinkConf]
    val _env = StreamExecutionEnvironment.getExecutionEnvironment
    _env.enableCheckpointing(flinkConf.interval)
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(flinkConf.retry,flinkConf.retryDelay))
    _env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500)
    env.getCheckpointConfig.setCheckpointTimeout(60000)
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    if (flinkConf.parallelCount > 0)
      _env.setParallelism(flinkConf.parallelCount)
    _env
  }

  def prepare(): DataStream[InputRawData]

  private def checkConfig(): Unit = {
    val inputConf = conf.asInstanceOf[FlinkConf]
    if (inputConf == null) throw new EventflowException("conf must be inherited from class [FlinkConf]")
  }
}