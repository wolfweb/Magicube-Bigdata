package com.magicube.eventflows

import org.junit.Test

class ScalaFuncTest {
  @Test
  def func_Demo_Test(): Unit = {
    Speaker To Weather(() => "today") Do "hello"
    Speaker To Human(() => "lilei") Do "nice to meet you"
  }
}


object Speaker {
  def To(el: => What): What = {
    el
  }

  def weather(el: => String): What = {
    Weather(el _)
  }
}

trait What {
  def Do[R](el: => R) = {
    println(el)
  }
}

case class Weather(someDay: () => String) extends What {
  print(s"${someDay.apply()} ")
}

case class Human(name: () => String) extends What {
  print(s"${name.apply()} ")
}