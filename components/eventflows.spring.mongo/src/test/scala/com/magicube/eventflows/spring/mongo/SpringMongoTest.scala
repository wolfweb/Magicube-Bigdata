package com.magicube.eventflows.spring.mongo

import java.sql.Timestamp

import com.magicube.eventflows.Repository.IEntityBase
import org.junit.Test
import org.springframework.context.annotation.Configuration

@Configuration
case class MongodbConfiguration() extends MongoConfiguration {
  override val conf: MongoConf = MongoConf("localhost", 27017, "demo")
}

case class Foo
(
  var id: Long,
  var Name: String,
  var CreateAt: Timestamp
) extends IEntityBase[Long]

class SpringMongoTest {
  @Test
  def func_mongo_test(): Unit = {
    val rep = MongoEntrance().repository[Foo, Long]
    assert(rep != null)
  }
}
