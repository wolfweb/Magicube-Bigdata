package com.magicube.eventflows.core.Format

import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.Json.JSON._
import com.magicube.eventflows.core.InputRawData
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.json4s.DefaultFormats

case class JsonDictFormatConf( )

class JsonDictFormat extends FormatComponent {
  private var jsonConf: JsonDictFormatConf = _

  override val order: Int = 9998

  override def initConfig(): Unit = {
    val path = "service.format.jsondict"
    if (conf.hasPath(path)) {
      jsonConf = conf.as[JsonDictFormatConf]
      //if (jsonDictFormat == null) throw EventflowException("json dict data format require delimiter .")
    }
  }

  override def formatData(stream: DataStream[InputRawData]): DataStream[InputRawData] = {
    stream.map(x => {
      if (x.rawStr != null) {
        val keypair = deserialize[Map[String, Any]](x.rawStr, DefaultFormats)
        for (it <- keypair) {
          x.datas.put(it._1, it._2)
        }
      }
      x
    })
  }
}
