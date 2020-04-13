package com.magicube.eventflows

import com.magicube.eventflows.Net.Curl
import org.junit.Test

class CurlTest {
  @Test
  def Func_Get_Test(): Unit = {
    val v = Curl.get("https://www.ichuye.cn").readAsString
    assert(v != "")
  }

  @Test
  def func_for_byStep():Unit={
    for(i<-10 to 1 by -1){
      println(i)
    }
  }
}

