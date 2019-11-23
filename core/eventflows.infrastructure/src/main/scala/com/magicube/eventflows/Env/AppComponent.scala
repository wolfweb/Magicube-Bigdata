package com.magicube.eventflows.Env

import java.io.{FileInputStream, InputStreamReader}

import org.apache.flink.streaming.api.scala._

import scala.reflect.ClassTag

trait AppComponent{
  def LoadConfig[T <: BaseAppConfig : ClassTag](file: String)(implicit ct: ClassTag[T]): Unit = {
    val fs = new FileInputStream(file)
    val reader = new InputStreamReader(fs, "utf8")
    val config = try {
      parseConfig(reader)
    } finally {
      reader.close()
      fs.close()
    }
    AppContext.init(config)
  }

  def parseConfig(reader: InputStreamReader): BaseAppConfig

  def Run(env: StreamExecutionEnvironment): Unit
}
