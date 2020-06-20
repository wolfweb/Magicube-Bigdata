package com.magicube.eventflows

import java.sql.Timestamp

import com.magicube.eventflows.Date.DateFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test

class DateFactoryTest {
  @Test
  def Func_DateFactory_Test(): Unit = {
    val date: DateTime = "2019/11/10 19:10:42"
    assert(date == new DateTime(2019, 11, 10, 19, 10, 42))
  }

  @Test
  def func_subSecond_test(): Unit = {
    var v = DateFactory.subSeconds("2019/11/29 01", "2019/11/29")
    assert(v == 3600)

    v = DateFactory.subSeconds("2019/11/29 00:01", "2019/11/29")
    assert(v == 60)

    v = DateFactory.subSeconds("2019/11/29 00:00:01", "2019/11/29")
    assert(v == 1)
  }

  @Test
  def func_regex_test(): Unit = {
    val str = "2019-12-02T11:32:28Z"
    var date: DateTime = str
    assert(date == DateTime.parse(str, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")))

    val timeStamp: Timestamp = date
    assert(timeStamp != null && timeStamp.toString == "2019-12-02 19:32:28.0")

    date = timeStamp
    assert(date == DateTime.parse(str, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")))

    var dateStr: String = date
    assert(dateStr == "2019-12-02T11:32:28")

    dateStr = timeStamp
    assert(dateStr == "2019-12-02T11:32:28")

    val timestamp: Long = date
    assert(timestamp == date.getMillis)
    date = timestamp
    assert(date == DateTime.parse(str, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")))
  }
}

