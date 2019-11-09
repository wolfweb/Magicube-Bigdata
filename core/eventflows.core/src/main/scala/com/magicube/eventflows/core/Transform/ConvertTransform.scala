package com.magicube.eventflows.core.Transform

import com.magicube.eventflows.Date.DateFactory._
import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core.InputRawData
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.streaming.api.scala._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class ConvertTransformConf(fields: List[ConvertField])

case class ConvertField(field: String, fieldType: String, formatPattern: String = "")

class ConvertTransform extends TransformComponent {
  private var convertConf: ConvertTransformConf = _
  override val order: Int = 9998

  override def initConfig(): Unit = {
    val path = "service.transform.convert"
    if (conf.hasPath(path)) {
      convertConf = conf.as[ConvertTransformConf](path)
      if (convertConf.fields.isEmpty) throw new EventflowException(s"convert config need convert field item ...")
    }
  }

  override def transformData(stream: DataStream[InputRawData]): DataStream[InputRawData] = {
    stream.map(x => {
      for (field <- convertConf.fields) {
        if (x.datas.containsKey(field.field)) {
          field.fieldType match {
            case "int" => x.datas.replace(field.field, x.datas.get(field.field).toString.toInt)
            case "long" => x.datas.replace(field.field, x.datas.get(field.field).toString.toLong)
            case "float" => x.datas.replace(field.field, x.datas.get(field.field).toString.toFloat)
            case "bool" => x.datas.replace(field.field, x.datas.get(field.field).toString.toBoolean)
            case "double" => x.datas.replace(field.field, x.datas.get(field.field).toString.toDouble)
            case "datetime" => x.datas.replace(field.field, parseTime(x.datas.get(field.field).toString, DateTimeFormat.forPattern(field.formatPattern)))
            case "secondToDate" => x.datas.replace(field.field, new DateTime(x.datas.get(field.field).toString.toLong * 1000))
            case "timestampToDate" => x.datas.replace(field.field, new DateTime(x.datas.get(field.field).toString.toLong))
            case _ => x.datas.replace(field.field, x.datas.get(field.field).toString)
          }
        }
      }
      x
    })
  }
}
