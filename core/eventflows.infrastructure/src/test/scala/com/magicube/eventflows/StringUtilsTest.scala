package com.magicube.eventflows

import org.junit.Test

class StringUtilsTest {
  @Test
  def func_String_Test(): Unit = {
    var str: String = null
    assert(!StringUtils.hasLength(str))
  }
}


