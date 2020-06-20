package com.magicube.eventflows.Date

import com.magicube.eventflows._
import org.joda.time.{DateTime, Seconds}
import org.json4s.{CustomSerializer, JString}

object DateFactory {
  def subSeconds(dateTime: DateTime, beginTime: DateTime = DateTime.now): Int = {
    Seconds.secondsBetween(beginTime, dateTime).getSeconds
  }

  object DatetimeSerializer extends CustomSerializer[DateTime](format => ( {
    case x: JString => x.s
  }, {
    case zdt: DateTime => JString(zdt.toString)
  }))

}


