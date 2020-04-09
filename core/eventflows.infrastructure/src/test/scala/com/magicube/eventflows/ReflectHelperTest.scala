package com.magicube.eventflows

import org.junit.Test

import scala.annotation.StaticAnnotation

class ReflectHelperTest {
  @Test
  def func_reflect_test(): Unit = {
    var attr = ReflectHelper.classAnnotation[Model, Attr]
    assert(attr != None && attr.get.name == "class")

    attr = ReflectHelper.memberAnnotation[Model, Attr]("say")
    assert(attr != None && attr.get.name == "m_say")

    attr = ReflectHelper.memberAnnotation[Model, Attr]("name")
    assert(attr != None && attr.get.name == "m_name")
  }
}

case class Attr(name: String) extends StaticAnnotation

@Attr("class")
case class Model
(
  @Attr("m_name")
  var name: String
) {
  @Attr("m_say")
  def say = "hello"
}