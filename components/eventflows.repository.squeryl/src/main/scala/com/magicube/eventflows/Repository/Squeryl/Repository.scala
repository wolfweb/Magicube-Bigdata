package com.magicube.eventflows.Repository.Squeryl

import org.slf4j.LoggerFactory
import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.dsl.ast._
import org.squeryl.dsl.boilerplate.FromSignatures

case class Repository[T <: IEntity[Long]](adapter: EntityDatabaseAdapter, tbName: String = null)(implicit manifestT: Manifest[T]) extends RepositoryBase[Long, T] {
  override var schema: EntitySchema = DbSchema(adapter)

  override def idToTypedExpressionNode(id: Long): TypedExpressionNode[_] = id

  override def table: Table[T] = if (tbName != null) schema.Table[T](tbName) else schema.Table[T]()
}

abstract class RepositoryBase[K, T <: IEntity[K]]()(implicit manifestT: Manifest[T]) extends FromSignatures {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  var schema: EntitySchema

  protected var _table: Option[Table[T]] = None

  def schemaName: String = schema.schemaName

  def table: Table[T] = _table.getOrElse(throw new IllegalAccessException("Set Table!!"))

  def repo: Table[T] = this.table

  def tableName: String = table.name

  def idToTypedExpressionNode(v: K): TypedExpressionNode[_]

  def resetAutoIncrement() {
    transaction {
      val query = schema.dbAdapter.autoInclementSql(tableName)
      val con = Session.currentSession.connection
      val statement = con.prepareStatement(query)
      statement.execute()
      con.commit()
    }
  }

  def all: Seq[T] = inTransaction {
    logger.debug(repo.statement)
    repo.toSeq
  }

  def create(entity: T) = inTransaction {
    table.insert(entity)
    this
  }

  def countAll: Long = inTransaction {
    val query = from(repo)(e => compute(count(idToTypedExpressionNode(e.id))))
    logger.debug(query.statement)
    query.toLong
  }

  def countBy(whereClauseFunctor: T => LogicalBoolean): Long = inTransaction {
    val query = from(repo)(e => where(whereClauseFunctor(e)) compute (count(idToTypedExpressionNode(e.id))))
    logger.debug(query.statement)
    val result: Long = query
    result
  }

  def delete(id: K): Boolean = inTransaction {
    table.delete(id)
  }

  def deleteAll(): Long = inTransaction {
    repo.deleteWhere(e => 1 === 1)
  }

  def deleteAll(whereClauseFunctor: T => LogicalBoolean): Long = inTransaction {
    repo.deleteWhere(whereClauseFunctor)
  }

  def exists(id: K): Boolean = inTransaction {
    repo.lookup(id) match {
      case Some(value) => true
      case _ => false
    }
  }

  def findById(id: K): Option[T] = inTransaction {
    logger.debug(s"find $tableName by id = $id")
    val e = repo.lookup(id)
    e.asInstanceOf[Option[T]]
  }

  def find(whereClauseFunctor: T => LogicalBoolean)(implicit dsl: QueryDsl): Seq[T] = inTransaction {
    val query = repo.where(whereClauseFunctor)(dsl)
    logger.debug(query.statement)
    query.toSeq
  }

  def find(whereClauseFunctor: T => LogicalBoolean, orderByFunctor: T => ExpressionNode)(implicit dsl: QueryDsl): Seq[T] = inTransaction {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (orderByFunctor(e)))
    logger.debug(query.statement)
    query.toSeq
  }

  def find
  (
    whereClauseFunctor: T => LogicalBoolean,
    orderByFunctor1: T => ExpressionNode,
    orderByFunctor2: T => ExpressionNode
  )(implicit dsl: QueryDsl): Seq[T] = inTransaction {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy(orderByFunctor1(e), orderByFunctor2(e)))

    logger.debug(query.statement)
    query.toSeq
  }

  def fetch(page: Int, pageLength: Int): Seq[T] = inTransaction {
    val query = from(repo)(e =>
      where(1 === 1)
        select (e)
        orderBy (idToTypedExpressionNode(e.id) asc))
      .page((page - 1) * pageLength, pageLength)
    logger.debug(query.statement)
    query.toSeq
  }

  def fetch(whereClauseFunctor: T => LogicalBoolean)(page: Int, pageLength: Int)(implicit dsl: QueryDsl): Seq[T] = inTransaction {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (idToTypedExpressionNode(e.id) asc))
      .page((page - 1) * pageLength, pageLength)
    logger.debug(query.statement)
    query.toSeq
  }

  def fetch(whereClauseFunctor: T => LogicalBoolean, orderByFunctor: T => ExpressionNode)(page: Int, pageLength: Int)(implicit dsl: QueryDsl): Seq[T] = inTransaction {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (orderByFunctor(e)))
      .page((page - 1) * pageLength, pageLength)
    logger.debug(query.statement)
    query.toSeq
  }

  def first(whereClauseFunctor: T => LogicalBoolean, orderByFunctor: T => ExpressionNode): Option[T] = inTransaction {
    val query = from(repo)(e =>
      where(whereClauseFunctor(e))
        select (e)
        orderBy (orderByFunctor(e))
    ).page(0, 1)
    logger.debug(query.statement)
    val e = query.single
    if (e != null) {
      Option(e)
    } else {
      None
    }
  }

  def save(entity: T): Boolean = inTransaction {
    table.insert(entity) != null
  }

  def update(entity: T) = inTransaction {
    table.update(entity)
    this
  }
}
