package com.magicube.eventflows.Repository.Squeryl

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class EntitySchema(adapter: EntityDatabaseAdapter) extends Schema {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  var schemaName: String = getClass.getSimpleName.replace("$", "")

  def dbAdapter: NativeQueryAdapter = adapter.nativeQueryAdapter

  def dataSource: HikariDataSource = adapter.DataSource

  def Table[T <: IEntity[_]]()(implicit manifestT: Manifest[T], ked: OptionalKeyedEntityDef[T, _]): Table[T] = super.table()

  def Table[T <: IEntity[_]](name: String)(implicit manifestT: Manifest[T], ked: OptionalKeyedEntityDef[T, _]): Table[T] = super.table(name)

  val sessionfactory = new SessionFactory {
    override def newSession: Session = createSession
  }

  def createSession: Session = {
    logger.debug(s"Create Session of ${dataSource.getJdbcUrl}")
    Session.create(dataSource.getConnection, adapter.adapter)
  }

  override def drop() = inTransaction(sessionfactory) {
    logger.debug(s"Drop:$schemaName")
    super.drop
  }

  override def create() = inTransaction(sessionfactory) {
    logger.debug(s"Create:$schemaName")
    printDdl
    super.create
  }
}