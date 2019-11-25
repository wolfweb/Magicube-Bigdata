package com.magicube.eventflows

import org.junit.Test

class ScalaFuncTest{
  @Test
  def func_Demo_Test(): Unit = {
    Speaker To Weather(Some(() => "today")) Do "hello"
    Speaker To Human(Some(() => "lilei")) Do "nice to meet you"
  }
}


object Speaker {
  def To(el: => What): What = {
    el
  }

  def weather(el: => String): What = {
    Weather(Some(el _))
  }
}

trait What {
  def Do[R](el: => R) = {
    println(el)
  }
}

case class Weather(someDay: Option[() => String]) extends What {
  print(s"${someDay.get.apply()} ")
}

case class Human(name: Option[() => String]) extends What {
  print(s"${name.get.apply()} ")
}