package com.magicube.eventflows

import com.magicube.eventflows.Net.Curl
import com.magicube.eventflows.Net.Curl.CurlExtension
import org.junit.Test

class CurlTest{
  @Test
  def Func_Get_Test():Unit={
    val v = Curl.get("http://localhost").readAsString
    assert(v=="hello")
  }
}
