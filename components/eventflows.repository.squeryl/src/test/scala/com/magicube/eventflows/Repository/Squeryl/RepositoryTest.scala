package com.magicube.eventflows.Repository.Squeryl

import java.sql.Timestamp
import java.util.UUID

import org.squeryl.PrimitiveTypeMode._
import com.magicube.eventflows._
import org.joda.time.DateTime
import org.junit.Test

class RepositoryTest {
  val localAdapter = MySql("jdbc:mysql://192.168.10.251:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8", "root", "123456")
  val remoteAdapter = MySql("jdbc:mysql://192.168.10.120:3306/chuyestatistics", "root", "111111")
  val localRep = Repository[Long, Foo](localAdapter)
  val remoteRep = Repository[Long, Foo](remoteAdapter, "demo")

  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"

  val threadPools = java.util.concurrent.Executors.newFixedThreadPool(2)

  @Test
  def func_rep_performance_test(): Unit = {
    for (i <- 0 to 10000) {
      val rep = Repository[Long, Foo](remoteAdapter, "demo")
      val entity = rep.first(x => x.id === 10, x => x.id desc)
      assert(entity == None)
    }
  }

  @Test
  def func_test(): Unit = {
    localRep.deleteAll()
    remoteRep.deleteAll()

    for (step <- 1 to 10) {
      localRep.create(Foo(UUID.randomUUID().toString, DateTime.now))
      remoteRep.create(Foo(UUID.randomUUID().toString, DateTime.now))
    }

    var entity = localRep.first(x => x.id gt 0, x => x.id desc)
    assert(entity != None)
    entity.get.Name = DateTime.now.toString("yyyy/MM/dd HH:mm:ss")
    localRep.update(entity.get)

    entity = remoteRep.first(x => x.id gt 0, x => x.id desc)
    assert(entity != None)
    entity.get.Name = DateTime.now.toString("yyyy/MM/dd HH:mm:ss")
    remoteRep.update(entity.get)

    entity = localRep.findById(10)
    assert(entity == None)

    entity = remoteRep.findById(10)
    assert(entity == None)

    entity = localRep.first(x => x.id === 10, x => x.id desc)
    assert(entity == None)

    entity = remoteRep.first(x => x.id === 10, x => x.id desc)
    assert(entity == None)

    var res = localRep.page(x => x.id gt 0, x => x.id desc)(1, 10)
    assert(res.size == 10)

    res = remoteRep.page(x => x.id gt 0, x => x.id desc)(1, 10)
    assert(res.size == 10)
  }
}

case class Foo
(
  var Name: String,
  CreateAt: Timestamp,
  var id: Long = 0
) extends EntityBase {
}
