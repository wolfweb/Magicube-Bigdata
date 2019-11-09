package com.magicube.eventflows.core.Input

import java.util

import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core.{FlinkConf, InputRawData}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.streaming.api.scala._

case class FileInputConf
(
  path: String
) extends FlinkConf {}

class FileInput extends InputComponent {
  private var fileConf: FileInputConf = null

  override def initConfig(): Unit = {
    val path = "server.input.file"
    if (conf.hasPath(path)) {
      fileConf = conf.as[FileInputConf](path)
      if (fileConf == null || fileConf.path == null || fileConf.path.isEmpty)
        throw EventflowException("file input require path .")
    }
  }

  override def prepare(): DataStream[InputRawData] = {
    val stream = env.readTextFile(fileConf.path)
    stream.map(x => InputRawData(x, new util.HashMap[String, Any]()))
  }
}
