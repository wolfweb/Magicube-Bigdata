package com.magicube.eventflows.Repository.Squeryl

import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.dsl.ast._

import scala.collection.mutable.{Map, Set}

case class Repository[K, T <: IEntity[K]](adapter: EntityDatabaseAdapter, tbName: String = null)(implicit manifestT: Manifest[T]) {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  val schema: EntitySchema = Repository.getOrCreateSchema(adapter)

  val schemaName: String = schema.schemaName

  val table: Table[T] = Repository.getOrCreateTable[K, T](tbName, adapter, manifestT) {
    if (tbName != null)
      schema.Table[T](tbName)
    else
      schema.Table[T]()
  }

  def tableName: String = table.name

  def repo: Table[T] = this.table

  def create(entity: T) = inTransaction(schema.sessionfactory) {
    table.insert(entity)
  }

  def delete(id: K)(implicit toCanLookup: K => CanLookup): Boolean = inTransaction(schema.sessionfactory) {
    table.delete(id)
  }

  def deleteAll(): Long = inTransaction(schema.sessionfactory) {
    repo.deleteWhere(e => 1 === 1)
  }

  def deleteAll(whereClauseFunctor: T => LogicalBoolean): Long = inTransaction(schema.sessionfactory) {
    repo.deleteWhere(whereClauseFunctor)
  }

  def findById(id: K)(implicit toCanLookup: K => CanLookup): Option[T] = inTransaction(schema.sessionfactory) {
    logger.debug(s"find $tableName by id = $id")
    val e = repo.lookup(id)
    e.asInstanceOf[Option[T]]
  }

  def find(whereClauseFunctor: T => LogicalBoolean)(implicit dsl: QueryDsl): List[T] = inTransaction(schema.sessionfactory) {
    val query = repo.where(whereClauseFunctor)(dsl)
    logger.debug(query.statement)
    query.toList
  }

  def find(whereClauseFunctor: T => LogicalBoolean, orderByFunctor: T => ExpressionNode)(implicit dsl: QueryDsl): List[T] = inTransaction(schema.sessionfactory) {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (orderByFunctor(e))
    )
    logger.debug(query.statement)
    query.toList
  }

  def find(pageSize: Int, skip: Int, whereClauseFunctor: T => LogicalBoolean, orderByFunctor: T => ExpressionNode)(implicit dsl: QueryDsl): List[T] = inTransaction(schema.sessionfactory) {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (orderByFunctor(e))
    ).page(skip, pageSize)
    logger.debug(query.statement)
    query.toList
  }

  def first(whereClauseFunctor: T => LogicalBoolean, orderByFunctor: T => ExpressionNode): Option[T] = inTransaction(schema.sessionfactory) {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (orderByFunctor(e))
    ).page(0, 1)
    logger.debug(query.statement)
    val e = if (query.size == 0) null.asInstanceOf[T] else query.single
    if (e != null) {
      Option(e)
    } else {
      None
    }
  }

  def page(whereClauseFunctor: T => LogicalBoolean, orderByFunctor: T => ExpressionNode)(pageIndex: Int, pageSize: Int)(implicit dsl: QueryDsl): List[T] = inTransaction(schema.sessionfactory) {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (orderByFunctor(e))
    ).page((pageIndex - 1) * pageSize, pageSize)
    logger.debug(query.statement)
    query.toList
  }

  def save(entity: T) = inTransaction(schema.sessionfactory) {
    table.insertOrUpdate(entity)
  }

  def update(entity: T) = inTransaction(schema.sessionfactory) {
    table.update(entity)
  }
}

object Repository {
  private var concreteTables: Set[AdapterTableComponent[_, _]] = Set()
  private var concreteFactory: Map[EntityDatabaseAdapter, EntitySchema] = Map[EntityDatabaseAdapter, EntitySchema]()

  def getOrCreateSchema(adapter: EntityDatabaseAdapter): EntitySchema = {
    val filters = concreteFactory.filter(x => x._1 == adapter)
    if (filters.nonEmpty) {
      filters.head._2
    } else {
      val schema = EntitySchema(adapter)
      concreteFactory += (adapter -> schema)
      schema
    }
  }

  def getOrCreateTable[K, T <: IEntity[K]](tbName: String, adapter: EntityDatabaseAdapter, manifest: Manifest[T])(el: => Table[T]): Table[T] = {
    var tableName = tbName
    if(tableName == null)
      tableName = manifest.getClass.getSimpleName

    val filters = concreteTables.filter(x => x.tbName == tableName && x.adapter == adapter && x.manifest == manifest)
    if (filters.nonEmpty)
      filters.head.table.asInstanceOf[Table[T]]
    else {
      val table = el
      concreteTables += AdapterTableComponent[K, T](tableName, adapter, manifest, table)
      table
    }
  }

  case class AdapterTableComponent[K, T <: IEntity[K]](tbName: String, adapter: EntityDatabaseAdapter, manifest: Manifest[T], table: Table[T])

}