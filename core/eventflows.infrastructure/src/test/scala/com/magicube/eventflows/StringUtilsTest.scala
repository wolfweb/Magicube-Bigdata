package com.magicube.eventflows

import org.junit.Test

class StringUtilsTest {
  @Test
  def func_String_Test(): Unit = {
    val str = "true"
    val res = str.isEmptyOrNull
    assert(!res)
  }
}


