package com.magicube.eventflows

import com.magicube.eventflows.Json.JSON._
import com.magicube.eventflows.SExpression._
import org.json4s.{DefaultFormats, FieldSerializer}
import org.json4s.FieldSerializer._
import org.junit.Test

class JsonTest {
  @Test
  def func_JsonToMap_Test(): Unit = {
    var str = "{\"gmt_create\":\"2019-07-01 12:49:15\",\"charset\":\"UTF-8\",\"buyer_id\":\"2088522413587515\",\"invoice_amount\":\"18.00\",\"notify_id\":\"2019070100222124916087510501287752\",\"fund_bill_list\":\"[{\\\"amount\\\":\\\"18.00\\\",\\\"fundChannel\\\":\\\"ALIPAYACCOUNT\\\"}]\",\"notify_type\":\"trade_status_sync\",\"trade_status\":\"TRADE_SUCCESS\",\"receipt_amount\":\"18.00\",\"app_id\":\"2019060465458247\",\"buyer_pay_amount\":\"18.00\",\"seller_id\":\"2088521196172033\",\"gmt_payment\":\"2019-07-01 12:49:16\",\"notify_time\":\"2019-07-01 12:49:16\",\"version\":\"1.0\",\"out_trade_no\":\"vpo-273fc5c165664cf0947b3c9e1c291332\",\"total_amount\":\"18.00\",\"trade_no\":\"2019070122001487510565398790\",\"auth_app_id\":\"2019060465458247\",\"buyer_logon_id\":\"136****5088\",\"point_amount\":\"0.00\"}"
    val keypair = deserialize[Map[String, Any]](str)
    var input = "(and (or (= trade_status TRADE_SUCCESS) (= trade_status TRADE_FINISHED)))"
    input = "(= total_amount 18.00)"
    val exp = Exp.from(SExp.from(input))
    println(Eval(keypair, exp))

    assert(keypair("trade_status").equals("TRADE_SUCCESS"))
  }

  @Test
  def func_Generic_Type_Test():Unit={
    assert(1.isPrimitive)
    assert("1".isPrimitive)
    assert(1.1.isPrimitive)
    assert(true.isPrimitive)
    assert(!CaseFoo(1,"wolfweb").isPrimitive)
  }

  @Test
  def func_Json_Test(): Unit = {
    val model = CaseFoo(1, "wolfweb")
    var str = serialize(model)
    val foo = deserialize[CaseFoo](str)
    assert(foo != null)
    assert(foo.id > 0)
    assert(foo.name == model.name)

    val normalModel = new Foo()
    normalModel.id = 1
    normalModel.name = "wolfweb"
    str = serialize(normalModel)

    val normal = deserialize[Foo](str)
    assert(normal != null)
    assert(normal.id > 0)
    assert(normal.name == model.name)

    val expected = deserialize[Foo]("{\"name\":\"wolfweb\",\"Id\":10}", FieldSerializer[Foo](
      null, //renameTo("id","Id") orElse renameTo("name","Name"),
      renameFrom("Id", "id") orElse renameFrom("Name", "name")
    ))
    assert(expected != null && expected.name == "wolfweb" && expected.id == 10)

    val serializeStr = serialize("1")
    assert(serializeStr == "\"1\"")
  }

  def Eval(datas: Map[String, Any], exp: Exp): Boolean = {
    var res = false
    if (exp.isInstanceOf[AndExp]) {
      val _exp = exp.asInstanceOf[AndExp]
      res = Eval(datas, _exp.cond1) && Eval(datas, _exp.cond2)
    } else if (exp.isInstanceOf[OrExp]) {
      val _exp = exp.asInstanceOf[OrExp]
      res = Eval(datas, _exp.cond1) || Eval(datas, _exp.cond2)
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
        if (_exp.exp2.isInstanceOf[IntExp]) {
          res = datas(k).toString.toInt.equals(_exp.exp2.toString.toInt)
        } else if (_exp.exp2.isInstanceOf[DoubleExp]) {
          res = datas(k).toString.toDouble.equals(_exp.exp2.toString.toDouble)
        } else {
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

case class CaseFoo
(
  id: Int,
  name: String
) {}

class Foo {
  var id: Int = _
  var name: String = _
}