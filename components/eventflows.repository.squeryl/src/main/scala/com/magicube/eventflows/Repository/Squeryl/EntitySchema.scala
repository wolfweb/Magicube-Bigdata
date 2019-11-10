package com.magicube.eventflows.Repository.Squeryl

import com.magicube.eventflows.Repository.Squeryl.Util.Companion
import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl._

trait EntitySchema extends Schema {
  protected val logger = LoggerFactory.getLogger(getClass.getName)
  var schemaName: String = getClass.getSimpleName.replace("$", "")

  private var _databaseAdapter: Option[EntityDatabaseAdapter] = None

  SessionFactory.concreteFactory = Some(sessionFactory)

  def databaseAdapter_=(adapter: EntityDatabaseAdapter) = _databaseAdapter = Some(adapter)

  def databaseAdapter: EntityDatabaseAdapter = _databaseAdapter.getOrElse(throw new IllegalStateException)

  def dbAdapter: NativeQueryAdapter = databaseAdapter.nativeQueryAdapter

  def dataSource: ComboPooledDataSource = databaseAdapter.DataSource

  def Table[T <: IEntity[_]]()(implicit manifestT: Manifest[T]): Table[T] = createTable {
    super.table()
  }

  def Table[T <: IEntity[_]](name: String)(implicit manifestT: Manifest[T]): Table[T] = createTable {
    super.table(name)
  }

  def newSession = SessionFactory.newSession

  def apply(adapter: EntityDatabaseAdapter): this.type = {
    this.databaseAdapter = adapter
    this
  }

  def createSession: Session = {
    logger.debug(s"Create Session of ${dataSource.getJdbcUrl}")
    Session.create(dataSource.getConnection, databaseAdapter.adapter)
  }

  protected def createTable[T <: IEntity[_]](func: => Table[T])(implicit manifestT: Manifest[T]): Table[T] = {
    Companion.of[T] match {
      case Some(companion) => {
        val t = func
        companion.asInstanceOf[RepositoryBase[_, T]].set(this, t)
        t
      }
      case None => throw new IllegalStateException(s"${manifestT.runtimeClass.getSimpleName}:Cannot find companion object")
    }
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

  def printSchema() {
    transaction {
      val query = dbAdapter.printSchemaSql
      val con = Session.currentSession.connection
      val statement = con.createStatement()
      val result = statement.executeQuery(query)
      while (result.next()) {
        println(s"${result.getString(1)} ${result.getString(2)} ${result.getString(3)}")
      }
    }
  }

  def sessionFactory: () => Session = { () => this.createSession }

  def setLogger(debug: String) = Session.currentSession.setLogger(s => println(s"$debug $s"))
}
