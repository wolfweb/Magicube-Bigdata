package com.magicube.eventflows.Repository.Squeryl

import java.util.UUID

import org.junit.Test

class RepositoryTest {
  @Test
  def func_test(): Unit = {
    val schema = Schema


    //schema.create()

    for(step<-1 to 10){
      Foo(step+7,UUID.randomUUID().toString).create()
    }

    for(step<-1 to 10) {
      val res = Foo.findById(1)
      println(res)
    }
  }
}

case class Foo(Id: Long, Name: String) extends EntityBase {
  override def id: Long = Id
}

object Foo extends Repository[Foo] {
  override var schema: EntitySchema = Schema
}


object Schema extends EntitySchema{
  databaseAdapter = MySql("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8","root","123456")
  val Users = Table[Foo]
}