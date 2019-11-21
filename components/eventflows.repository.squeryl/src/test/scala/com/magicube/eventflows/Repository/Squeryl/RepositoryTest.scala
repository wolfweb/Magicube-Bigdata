package com.magicube.eventflows.Repository.Squeryl

import java.sql.Timestamp
import java.util.UUID

import com.magicube.eventflows._
import org.joda.time.DateTime
import org.junit.Test

class RepositoryTest {
  val localAdapter = MySql("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8", "root", "123456")
  val remoteAdapter = MySql("jdbc:mysql://192.168.10.120:3306/chuyestatistics", "root", "111111")
  val localRep = Repository[Foo](localAdapter)
  val remoteRep = Repository[Foo](remoteAdapter, "demo")

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"

  val threadPools = java.util.concurrent.Executors.newFixedThreadPool(2)

  @Test
  def func_test(): Unit = {
    func_rep_test(localRep)
    func_rep_test(remoteRep)
  }

  def func_rep_test(rep: Repository[Foo]): Unit = {
    rep.deleteAll()
    for (step <- 1 to 10) {
      rep.create(Foo(step, UUID.randomUUID().toString, DateTime.now.toString(dateFormat).toSqlTime(dateFormat)))
    }

    for (step <- 1 to 10) {
      val res = rep.findById(step)
      println(res)
    }
  }
}

case class Foo
(
  Id: Long,
  var Name: String,
  CreateAt: Timestamp
) extends EntityBase {
  override def id: Long = Id
}
