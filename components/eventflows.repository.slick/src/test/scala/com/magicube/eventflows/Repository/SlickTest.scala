package com.magicube.eventflows.Repository

import com.magicube.eventflows.Repository.Slick.Conf.{Config, JdbcConfig, MySQLConfig}
import com.magicube.eventflows.Repository.Slick.Keyed
import com.magicube.eventflows.Repository.Slick.Repository.Repository
import org.junit.Test
import slick.ast.BaseTypedType

import scala.concurrent.ExecutionContext.Implicits.global

case class Person(override val id: Option[Int] = None, name: String) extends Entity[Person, Int] {
  def withId(id: Int): Person = this.copy(id = Some(id))
}

case class PersonRepository(conf: Config) extends Repository[Person, Int](conf) {

  import driver.api._

  override type TableType = Persons

  override def pkType = implicitly[BaseTypedType[Int]]

  override def tableQuery = TableQuery[Persons]

  class Persons(tag: slick.lifted.Tag) extends Table[Person](tag, "PERSON") with Keyed[Int] {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)

    def name = column[String]("NAME")

    def * = (id.?, name) <> ((Person.apply _).tupled, Person.unapply)
  }

}

class SlickTest {
  @Test
  def Func_Repository_Test(): Unit = {
    val conf = MySQLConfig(JdbcConfig("jdbc:mysql://192.168.10.122:3306/matomo", "com.mysql.jdbc.Driver", "root", "111111"))
    val rep = PersonRepository(conf.config)
    import rep._
    executeAction(rep.save(Person(Some(1), "wolfweb")))
    val datas = executeAction(rep.findAll())
    println(datas)
  }
}
