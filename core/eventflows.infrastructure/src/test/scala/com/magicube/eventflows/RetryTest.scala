package com.magicube.eventflows

import org.junit.Test

object RetryTest {
  var count = 0
}

class RetryTest {
  @Test
  def func_Retry_Test(): Unit = {
    Retry.retry(6, 1000)(func(RetryTest.count), func1())
  }

  private def func1(): Unit = {
    RetryTest.count += 1
  }

  private def func(i: Int): Unit = {
    println(i)
    if (i < 5) throw new RuntimeException("test")
    println(s"exec times: ${i}")
  }
}
