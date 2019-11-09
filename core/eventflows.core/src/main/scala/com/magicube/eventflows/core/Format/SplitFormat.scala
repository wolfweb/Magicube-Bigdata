package com.magicube.eventflows.core.Format

import java.net.URLDecoder

import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core.Format.SplitFormatMode.SplitFormatMode
import com.magicube.eventflows.core.InputRawData
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader._
import org.apache.flink.streaming.api.scala._

case class SplitFormatConf
(
  splitMode: SplitFormatMode,
  columns: Array[String],
  var delimiter: String = ","
)

object SplitFormatMode extends Enumeration {
  type SplitFormatMode = Value
  val QueryString = Value(0)
}

class SplitFormat extends FormatComponent {
  private var splitConf: SplitFormatConf = _

  override val order: Int = 9999

  override def initConfig(): Unit = {
    val path = "service.format.split"
    if (conf.hasPath(path)) {
      splitConf = conf.as[SplitFormatConf](path)
      if (splitConf == null) throw EventflowException("split data format require delimiter .")
      if (splitConf.delimiter == null) splitConf.delimiter = ","
    }
  }

  override def formatData(stream: DataStream[InputRawData]): DataStream[InputRawData] = {
    if (splitConf.splitMode != null) {
      splitConf.splitMode match {
        case SplitFormatMode.QueryString => stream.map(x => queryStringFormat(x))
        case _ => stream.map(x => splitDataFormat(x))
      }
    } else {
      stream.map(x => splitDataFormat(x))
    }
  }

  private def splitDataFormat(v: InputRawData): InputRawData = {
    if (v.rawStr != null) {
      val pairs = v.rawStr.split(splitConf.delimiter).map(_.trim)
      for (i <- 0 to pairs.size) {
        if (splitConf.columns != null && splitConf.columns.length == pairs.size)
          v.datas.put(splitConf.columns(i), pairs(i))
        else
          v.datas.put(i.toString, pairs(i))
      }
    }
    v
  }

  private def queryStringFormat(v: InputRawData): InputRawData = {
    if (v.rawStr != null) {
      val pairs = v.rawStr.split("&")
      for (pair: String <- pairs) {
        val idx = pair.indexOf("=")
        v.datas.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"))
      }
    }
    v
  }
}