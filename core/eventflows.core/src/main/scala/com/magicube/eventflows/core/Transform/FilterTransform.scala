package com.magicube.eventflows.core.Transform

import com.magicube.eventflows.SExpression._
import com.magicube.eventflows.core.InputRawData
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import org.apache.flink.streaming.api.scala.DataStream

case class FilterTransformConf
(
  groups: List[Groupby]
)

case class Groupby
(
  forMeta: Boolean,
  key: String,
  value: String
)

class FilterTransform extends TransformComponent {
  private var filterConf: FilterTransformConf = _
  override val order: Int = 9999

  override def initConfig(): Unit = {
    val path = "service.transform.filter"
    if (conf.hasPath(path)) {
      filterConf = conf.as[FilterTransformConf](path)
    }
  }

  override def transformData(stream: DataStream[InputRawData]): DataStream[InputRawData] = {
    //todo:
    for (group <- filterConf.groups) {
      stream.filter(x => {
        if (group.forMeta && x.metadata.containsKey(group.key)) {

        } else {

        }
        false
      })
    }

    val res = stream.filter(x => {

      false
    })

    null
  }

  protected def eval(datas: Map[String, Any], exp: Exp): Boolean = {
    var res = false
    if (exp.isInstanceOf[AndExp]) {
      val _exp = exp.asInstanceOf[AndExp]
      res = eval(datas, _exp.cond1) && eval(datas, _exp.cond2)
    } else if (exp.isInstanceOf[OrExp]) {
      val _exp = exp.asInstanceOf[OrExp]
      res = eval(datas, _exp.cond1) || eval(datas, _exp.cond2)
    } else if (exp.isInstanceOf[ContainsExp]) {
      val _exp = exp.asInstanceOf[ContainsExp]
      val k = _exp.car.toString
      if (datas.contains(k)) {
        res = datas(k).toString.contains(_exp.cdr.toString)
      }
    } else if (exp.isInstanceOf[EqExp]) {
      val _exp = exp.asInstanceOf[EqExp]
      val k = _exp.exp1.toString
      if (datas.contains(k)) {
        if(_exp.exp2.isInstanceOf[IntExp]){
          res = datas(k).toString.toInt.equals(_exp.exp2.toString.toInt)
        } else if(_exp.exp2.isInstanceOf[DoubleExp]){
          res = datas(k).toString.toDouble.equals(_exp.exp2.toString.toDouble)
        } else{
          res = datas(k).equals(_exp.exp2.toString)
        }
      }
    } else if (exp.isInstanceOf[GreaterThan]) {
      val _exp = exp.asInstanceOf[GreaterThan]
      val k = _exp.exp1.toString
      if (datas.contains(k)) {
        res = datas(k).toString.toDouble > _exp.exp2.toString.toDouble
      }
    } else if (exp.isInstanceOf[GreaterEqThan]) {
      val _exp = exp.asInstanceOf[GreaterEqThan]
      val k = _exp.exp1.toString
      if (datas.contains(k)) {
        res = datas(k).toString.toDouble >= _exp.exp2.toString.toDouble
      }
    } else if (exp.isInstanceOf[LessThan]) {
      val _exp = exp.asInstanceOf[LessThan]
      val k = _exp.exp1.toString
      if (datas.contains(k)) {
        res = datas(k).toString.toDouble < _exp.exp2.toString.toDouble
      }
    } else if (exp.isInstanceOf[LessEqThan]) {
      val _exp = exp.asInstanceOf[LessEqThan]
      val k = _exp.exp1.toString
      if (datas.contains(k)) {
        res = datas(k).toString.toDouble <= _exp.exp2.toString.toDouble
      }
    }
    res
  }
}

