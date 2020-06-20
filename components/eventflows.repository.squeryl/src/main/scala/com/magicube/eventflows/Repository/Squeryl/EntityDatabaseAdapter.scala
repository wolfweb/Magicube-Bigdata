package com.magicube.eventflows.Repository.Squeryl

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import org.squeryl.adapters._
import org.squeryl.internals.DatabaseAdapter

abstract class EntityDatabaseAdapter(url: String, driver: String, user: String = null, password: String = null) {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  val DataSource: HikariDataSource = createDateSource(url, driver, user, password)
  val adapter: DatabaseAdapter

  def createDateSource(url: String, driver: String, user: String = "", password: String = ""): HikariDataSource = {
    logger.info("Creating connection with c3po connection pool")
    Class.forName(driver)
    val ds = new HikariDataSource()
    ds.setDriverClassName(driver)
    ds.setJdbcUrl(url)
    ds.setUsername(user)
    ds.setPassword(password)
    ds
  }

  def nativeQueryAdapter: NativeQueryAdapter = {
    adapter match {
      case _: MySQLAdapter => MySqlDBAdapter
      case _: H2Adapter => H2DBAdapter
      case _: MSSQLServer => MSSqlAdapter
      case _ => null
    }
  }
}