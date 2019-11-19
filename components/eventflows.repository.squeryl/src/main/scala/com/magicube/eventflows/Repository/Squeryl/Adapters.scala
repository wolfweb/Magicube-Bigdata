package com.magicube.eventflows.Repository.Squeryl

import org.squeryl.adapters._

case class MariaDB(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "org.mariadb.jdbc.Driver", user, password) {
  val adapter = new MySQLInnoDBAdapter
}

case class H2(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "org.h2.Driver", user, password) {
  val adapter = new H2Adapter
}

case class DB2(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "com.ibm.db2.jcc.DB2Driver", user, password) {
  val adapter = new DB2Adapter
}

case class Derby(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "org.apache.derby.jdbc.EmbeddedDriver", user, password) {
  val adapter = new DerbyAdapter
}

case class MSSQLServer(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "sqljdbc.jar, sqljdbc4.ja", user, password) {
  type MSSQLAdapter = org.squeryl.adapters.MSSQLServer
  val adapter = new MSSQLAdapter
}

case class MySql(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "com.mysql.cj.jdbc.Driver", user, password) {
  val adapter = new MySQLAdapter
}

case class MySqlInnoDB(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "com.mysql.jdbc.Driver", user, password) {
  val adapter = new MySQLInnoDBAdapter
}

case class Oracle(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "oracle.jdbc.driver.OracleDriver", user, password) {
  val adapter = new OracleAdapter
}

case class PostgreSql(url: String, user: String = null, password: String = null)
  extends EntityDatabaseAdapter(url, "org.postgresql.Driver", user, password) {
  val adapter = new PostgreSqlAdapter
}
