package com.magicube.eventflows.spring.jdbc

import com.magicube.eventflows.Repository.DatabaseAdapter
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.data.relational.core.mapping.{NamingStrategy, RelationalPersistentProperty}

@Configuration
abstract class JdbcConfiguration() extends AbstractJdbcConfiguration {
  val adapter: DatabaseAdapter

  @Bean
  def dataSource: DataSource = {
    val ds = new HikariDataSource()
    ds.setDriverClassName(adapter.driver)
    ds.setJdbcUrl(adapter.url)
    ds.setUsername(adapter.user)
    ds.setPassword(adapter.pwd)
    ds
  }

  @Bean
  def namingStrategy: NamingStrategy = {
    new NamingStrategy() {
      private var clasz: Class[_] = null

      override def getColumnName(property: RelationalPersistentProperty): String = {
        property.getName
      }

      override def getTableName(`type`: Class[_]): String = {
        if(clasz == null) clasz = `type`
        `type`.getSimpleName
      }
    }
  }
}