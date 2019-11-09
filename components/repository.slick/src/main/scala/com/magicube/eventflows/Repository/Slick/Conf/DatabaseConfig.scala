package com.magicube.eventflows.Repository.Slick.Conf

import slick.jdbc._

abstract class DatabaseConfig {
  def config: Config
}

case class H2Config(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("h2", conf, H2Profile, Error(23505, "23505"), "select 1", Error(50200, "HYT00"))
}

case class MySQLConfig(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("mysql", conf, MySQLProfile, Error(1062, "23000"), "select 1", Error(1213, "40001"))
}

case class OracleConfig(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("oracle", conf, OracleProfile, Error(1, "23000"), "select 1 from dual", Error(60, "61000"))
}

case class DB2Config(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("db2", conf, DB2Profile, Error(-803, "23505"), "select 1 from sysibm.sysdummy1", Error(-911, "40001"))
}

case class PostgresConfig(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("postgres", conf, PostgresProfile, Error(0, "23505"), "select 1", Error(0, "40P01"))
}

case class SQLServerConfig(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("sqlserver", conf, SQLServerProfile, Error(2627, "23000"), "select 1", Error(1205, "40001"))
}

case class DerbyConfig(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("derby", conf, DerbyProfile, Error(20000, "23505"), "values 1", Error(30000, "40001"))
}

case class HsqlConfig(conf: JdbcConfig) extends DatabaseConfig {
  override def config: Config = Config("hsql", conf, HsqldbProfile, Error(-104, "23505"), "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS", Error(-4861, "40001"))
}
