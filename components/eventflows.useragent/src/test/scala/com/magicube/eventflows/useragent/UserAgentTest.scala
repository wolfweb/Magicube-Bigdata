package com.magicube.eventflows.useragent

import org.junit.Test

class UserAgentTest {
  @Test
  def func_useragent_test(): Unit = {
    val ua = "Mozilla/5.0 (Linux; Android 8.1.0; 16th Plus Build/OPM1.171019.026; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/65.0.3325.110 Mobile Safari/537.36 NetType/MOBILE"
    val datas = userAgentAnalyzer.parse(ua)
  }
}
