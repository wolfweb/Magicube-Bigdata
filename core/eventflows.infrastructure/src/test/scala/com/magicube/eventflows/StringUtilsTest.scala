package com.magicube.eventflows

import java.net.URLDecoder

import org.apache.commons.lang3.StringEscapeUtils
import org.junit.Test

class StringUtilsTest {
  @Test
  def func_String_Test(): Unit = {
    val str = "true"
    val res = str.isNullOrEmpty
    assert(!res)
  }

  @Test
  def func_String_Trim_Test(): Unit = {
    val str = "?true?"
    val res = str.trim("?")
    assert(res == "true")
  }

  @Test
  def func_String_ToDict_Test(): Unit = {
    val queryStr = "?abc=1&cde=&efg"
    val queryString = queryStr.trim("?")
    val dict = queryString.split("&").map(x => {
      val arr = x.split("=")
      val v = if (arr.size > 1) arr(1) else null
      arr(0) -> v
    }).toMap
    assert(dict("abc") == "1")
    assert(dict("cde") == null)
    assert(dict("efg") == null)
  }

  @Test
  def func_String_Decode_Test(): Unit = {
    var str = "?lastId\\u003d1\\u0026device\\u003dAndroid\\u0026w\\u003d1080\\u0026h\\u003d2139\\u0026version\\u003d4.6.0\\u0026nettp\\u003dMOBILE"
    var res = str.urlDecode
    assert(res == "?lastId=1&device=Android&w=1080&h=2139&version=4.6.0&nettp=MOBILE")
    str = "q=scala%20unicode%20unescapes%20&qs=n&form=QBRE&sp=-1&pq=scala%20unicode%20unescapes%20&sc=0-24&sk=&cvid=F9009DE0EEE04E62B11EDAFAC5C209AD"
    res = str.urlDecode
    assert(res=="q=scala unicode unescapes &qs=n&form=QBRE&sp=-1&pq=scala unicode unescapes &sc=0-24&sk=&cvid=F9009DE0EEE04E62B11EDAFAC5C209AD")
  }
}


