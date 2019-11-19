package com.magicube.eventflows.Repository.Squeryl

import java.util.UUID

import org.junit.Test

class RepositoryTest {
  val adapter = MySql("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8", "root", "123456")
  val rep = Repository[Foo](adapter)

  @Test
  def func_test(): Unit = {
    rep.deleteAll()
    for (step <- 1 to 10) {
      rep.create(Foo(step, UUID.randomUUID().toString))
    }

    for (step <- 1 to 10) {
      val res = rep.findById(step)
      println(res)
    }
  }
}

case class Foo(Id: Long, Name: String) extends EntityBase {
  override def id: Long = Id
}
