package com.magicube.eventflows.core.Output

import com.magicube.eventflows.core.InputRawData
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.streaming.api.scala.DataStream

case class PrintOutputConf()

class PrintOutputComponent extends OutputComponent {
  private var printConf: PrintOutputConf = _

  override def initConfig(): Unit = {
    val path = "service.outpout.print"
    if (conf.hasPath(path)) {
      printConf = conf.as[PrintOutputConf](path)
    }
  }

  override def sinkData(stream: DataStream[InputRawData]): Unit = {
    stream.print()
  }
}
