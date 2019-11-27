package com.magicube.eventflows.Repository.Squeryl

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

case class EntitySchema(adapter: EntityDatabaseAdapter) extends Schema {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  var schemaName: String = getClass.getSimpleName.replace("$", "")

  def dbAdapter: NativeQueryAdapter = adapter.nativeQueryAdapter

  def dataSource: ComboPooledDataSource = adapter.DataSource

  def Table[T <: IEntity[_]]()(implicit manifestT: Manifest[T]): Table[T] = super.table()

  def Table[T <: IEntity[_]](name: String)(implicit manifestT: Manifest[T]): Table[T] = super.table(name)

  val sessionfactory = new SessionFactory {
    override def newSession: Session = createSession
  }

  def createSession: Session = {
    logger.debug(s"Create Session of ${dataSource.getJdbcUrl}")
    Session.create(dataSource.getConnection, adapter.adapter)
  }

  override def drop() = inTransaction {
    logger.debug(s"Drop:$schemaName")
    super.drop
  }

  override def create() = inTransaction {
    logger.debug(s"Create:$schemaName")
    printDdl
    super.create
  }
}