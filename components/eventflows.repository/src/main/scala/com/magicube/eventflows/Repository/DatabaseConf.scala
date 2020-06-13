package com.magicube.eventflows.Repository

import com.magicube.eventflows.Repository.DatabaseType.DatabaseType

object DatabaseType extends Enumeration {
  type DatabaseType = Value
  val H2          = Value(0)
  val DB2         = Value(1)
  val Derby       = Value(2)
  val MySql       = Value(3)
  val MSSql       = Value(4)
  val Oracle      = Value(5)
  val MariaDB     = Value(6)
  val PostgreSql  = Value(7)
  val MySqlInnoDB = Value(8)
}

case class DatabaseConf
(
  url: String,
  user: String,
  pwd: String,
  dbType: DatabaseType
)
