package com.magicube.eventflows.spring.jdbc

import java.sql.Timestamp

import com.magicube.eventflows.Repository.{DatabaseAdapter, IEntityBase, MySql}
import org.junit.Test
import org.springframework.context.annotation.Configuration
import org.springframework.data.relational.core.mapping.{Column, Table}

import scala.collection.JavaConverters._

@Configuration
case class MySqlJdbcConfiguration() extends JdbcConfiguration {
  override val adapter: DatabaseAdapter = MySql("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true", "root", "123456")
}

@Table("foo")
case class Foo
(
  var id: Long,
  var Name: String,
  var CreateAt: Timestamp
) extends IEntityBase[Long]

class SpringJdbcTest {
  @Test
  def func_jdbc_test(): Unit = {
    val rep = JdbcEntrance().repository[Foo, Long]
    assert(rep != null)
    val datas = rep.operations.findAll(classOf[Foo])
    assert(datas.asScala.size > 0)
  }
}
