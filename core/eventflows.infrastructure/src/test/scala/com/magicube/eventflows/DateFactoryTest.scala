package com.magicube.eventflows

import com.magicube.eventflows.Date.DateFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test

import scala.util.matching.Regex

class DateFactoryTest {
  @Test
  def Func_DateFactory_Test(): Unit = {
    val date = DateFactory.parseTime("2019/11/10 19:10:42",DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss"))
    assert(date == new DateTime(2019,11,10,19,10,42))
  }

  @Test
  def func_regex_test(): Unit = {
    val str = "2019-12-02T11:32:28Z"
    val reg = new Regex("(\\d+)([/\\-]+)(\\d+)([/\\-]+)(\\d+)([T\\s]+)(\\d+)(:)(\\d+)(:)(\\d+)([\\.\\d+]*)(Z*)")
    val m = reg.findFirstMatchIn(str)
    val groups = m.get.subgroups.toList
    val builder = new StringBuilder
    builder ++= groups(0).flatMap(x => "y")
    builder ++= groups(1)
    builder ++= groups(2).flatMap(x => "M")
    builder ++= groups(3)
    builder ++= groups(4).flatMap(x => "d")
    val flag = if (groups(5) == "T") "'T'" else groups(5)
    builder ++= flag
    builder ++= groups(6).flatMap(x => "H")
    builder ++= groups(7)
    builder ++= groups(8).flatMap(x => "m")
    builder ++= groups(9)
    builder ++= groups(10).flatMap(x => "s")
    if (groups.size > 10) {
      val suffix = if (groups(11).startsWith(".")) groups(11).flatMap(x => {
        x match {
          case '.' => "."
          case _ => "S"
        }
      }).mkString else ""
      if (suffix != "") {
        builder ++= suffix
        builder ++= groups(12)
      } else {
        builder ++= groups(12)
      }
    }
    println(builder.mkString)
  }
}

