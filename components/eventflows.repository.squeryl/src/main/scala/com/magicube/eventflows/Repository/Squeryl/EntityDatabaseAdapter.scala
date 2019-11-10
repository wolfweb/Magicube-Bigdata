package com.magicube.eventflows.Repository.Squeryl

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import org.squeryl.adapters._
import org.squeryl.internals.DatabaseAdapter

abstract class EntityDatabaseAdapter(url: String, driver: String, user: String = null, password: String = null) {
  protected val logger = LoggerFactory.getLogger(getClass.getName)
  val DataSource: ComboPooledDataSource = createDateSource(url, driver, user, password)
  val adapter: DatabaseAdapter

  def createDateSource(url: String, driver: String, user: String = "", password: String = ""): ComboPooledDataSource = {
    logger.info("Creating connection with c3po connection pool")
    Class.forName(driver)
    val ds = new ComboPooledDataSource
    ds.setJdbcUrl(url)
    ds.setDriverClass(driver)
    ds.setUser(user)
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