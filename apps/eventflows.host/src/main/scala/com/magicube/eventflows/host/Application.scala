package com.magicube.eventflows.host

import com.magicube.eventflows.Env.AppComponent
import com.magicube.eventflows.host.Env.CommandArgs
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.apache.flink.streaming.api.scala._
import scopt.OptionParser

import scala.language.postfixOps

object Application {
  def main(args: Array[String]): Unit = {
    val parser = new OptionParser[CommandArgs]("eventflows") {
      head("EventFlows", "1.0.1")
      opt[String]('c', "conf").action((x, c) => c.copy(conf = x)).text("conf is app run config")
      opt[String]('k', "appName").action((x, c) => c.copy(appName = x)).text("appName is required")
    }

    val cmd: Option[CommandArgs] = parser.parse(args, new CommandArgs)

    val file = cmd.get.conf
    val appName = cmd.get.appName

    if (file.isEmpty)
      println("app run with on config!!!")

    if (appName.isEmpty) {
      println("appName is required,please set -k for appName first!")
      return
    }

    val currentDirectory = new java.io.File(".").getCanonicalPath

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(1000)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,1500))
    env.setParallelism(1)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500)
    env.getCheckpointConfig.setCheckpointTimeout(60000)
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)

    try {
      val service: AppComponent = Class.forName(s"com.magicube.eventflows.host.Apps.${appName}.${appName}Service").newInstance().asInstanceOf[AppComponent]
      println(s"start ${appName} service")
      service.LoadConfig(file)
      service.Run(env)

      env.execute("start eventflows service")
    } catch {
      case e: Exception => println(s"no app component found with ${e}")
      case e:ClassNotFoundException=> println(e)
    }
  }
}
