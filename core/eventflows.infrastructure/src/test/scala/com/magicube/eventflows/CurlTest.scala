package com.magicube.eventflows

import com.magicube.eventflows.Net.Curl
import org.junit.Test

class CurlTest{
  @Test
  def Func_Get_Test():Unit={
    val v = Curl.get("http://localhost").readAsString
    assert(v=="hello")
  }
}