package com.magicube.eventflows.Date

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, Seconds}
import org.json4s.{CustomSerializer, JString}

object DateFactory {
  @transient
  val TimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSS")

  def subTimeSecond(dateTime: DateTime, beginTime: DateTime = DateTime.now): Int = {
    Seconds.secondsBetween(beginTime, dateTime).getSeconds
  }

  def parseIosTime(str: String): DateTime = {
    ISODateTimeFormat.dateTime().parseDateTime(str)
  }

  def parseTime(str: String, formatter: DateTimeFormatter): DateTime = {
    DateTime.parse(str, formatter)
  }

  def timestampToDateTime(value: Long): DateTime = {
    new DateTime(value)
  }

  object DatetimeSerializer extends CustomSerializer[DateTime](format => ( {
    case x: JString => parseIosTime(x.s)
  }, {
    case zdt: DateTime => JString(zdt.toString)
  }))
}


