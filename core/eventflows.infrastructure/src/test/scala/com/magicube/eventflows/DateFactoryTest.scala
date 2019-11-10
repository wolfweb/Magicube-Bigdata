package com.magicube.eventflows

import com.magicube.eventflows.Date.DateFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test

class DateFactoryTest {
  @Test
  def Func_DateFactory_Test(): Unit = {
    val date = DateFactory.parseTime("2019/11/10 19:10:42",DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss"))
    assert(date == new DateTime(2019,11,10,19,10,42))
  }
}

