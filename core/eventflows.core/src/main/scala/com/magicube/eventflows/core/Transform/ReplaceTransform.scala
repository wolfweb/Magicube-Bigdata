package com.magicube.eventflows.core.Transform

import com.magicube.eventflows.Exceptions.EventflowException
import com.magicube.eventflows.core.InputRawData
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.streaming.api.scala._

import scala.util.matching.Regex

case class ReplaceTransformConf(fields: List[ReplaceItem])

case class ReplaceItem(field: String, replaceName: String = "", regResolve: String = "", defValue: String = "", primaryKey: Boolean = false)

class ReplaceTransform extends TransformComponent {
  private var replaceConf: ReplaceTransformConf = _

  override val order: Int = 9990

  override def initConfig(): Unit = {
    val path = "service.transform.replace"
    if (conf.hasPath(path)) {
      replaceConf = conf.as[ReplaceTransformConf](path)
      if (replaceConf.fields.isEmpty) throw new EventflowException(s"replace config need replace config item ...")
    }
  }

  override def transformData(stream: DataStream[InputRawData]): DataStream[InputRawData] = {
    stream.map(x => {
      for (field <- replaceConf.fields) {
        if (x.datas.containsKey(field.field)) {
          var key = field.field
          var v = x.datas.get(field.field).toString

          if (!field.replaceName.isEmpty) {
            key = field.replaceName
          }

          if (!field.regResolve.isEmpty) {
            var group = 0
            val reg = if (field.regResolve.contains("->")) {
              val arr = field.regResolve.split("->")
              group = arr(1).toInt
              new Regex(arr(0))
            }
            else new Regex(field.regResolve)
            val ms = reg.findFirstMatchIn(v).toArray
            if (ms.length > 0)
              v = ms(0).group(group)
            else {
              if (field.defValue != null)
                v = field.defValue
              else
                v = "unknown"
            }
          }

          if (!x.datas.containsKey(key)) {
            x.datas.put(key, v)
          }
        }
      }
      x
    })
  }
}
