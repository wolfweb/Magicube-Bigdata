package com.magicube.eventflows.Repository

abstract class DatabaseAdapter() {
  val url: String
  val user: String
  val driver: String
  val pwd: String
}

case class MySql(val url: String, val user: String = null, val pwd: String = null) extends DatabaseAdapter() {
  val driver = "com.mysql.cj.jdbc.Driver"
}

case class MySqlInnoDB(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "com.mysql.jdbc.Driver"
}

case class Oracle(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "oracle.jdbc.driver.OracleDriver"
}

case class PostgreSql(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "org.postgresql.Driver"
}

case class MSSQLServer(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "sqljdbc.jar, sqljdbc4.ja"
}

case class MariaDB(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "org.mariadb.jdbc.Driver"
}

case class H2(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "org.h2.Driver"
}

case class DB2(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "com.ibm.db2.jcc.DB2Driver"
}

case class Derby(url: String, user: String = null, pwd: String = null) extends DatabaseAdapter() {
  val driver = "org.apache.derby.jdbc.EmbeddedDriver"
}