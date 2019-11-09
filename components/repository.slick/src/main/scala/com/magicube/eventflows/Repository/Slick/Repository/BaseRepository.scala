package com.magicube.eventflows.Repository.Slick.Repository

import com.magicube.eventflows.Repository.Entity
import com.magicube.eventflows.Repository.Slick.Annotation._
import com.magicube.eventflows.Repository.Slick.Keyed
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile
import slick.lifted.AppliedCompiledFunction
import slick.relational.RelationalProfile

import scala.annotation.StaticAnnotation
import scala.concurrent.ExecutionContext

trait BaseRepository[T <: Entity[T, Id], Id] {
  protected val driver: JdbcProfile

  import driver.api._

  type TableType <: Keyed[Id] with RelationalProfile#Table[T]

  def pkType: BaseTypedType[Id]

  implicit lazy val _pkType: BaseTypedType[Id] = pkType

  def tableQuery: TableQuery[TableType]

  type F = AppliedCompiledFunction[_, Query[TableType, T, Seq], Seq[T]]

  def findAll()(implicit ec: ExecutionContext): DBIO[Seq[T]] = {
    tableQueryCompiled.result.map(seq => sequenceLifecycleEvent(seq, _postLoad, classOf[postLoad]))
  }

  def findOne(id: Id)(implicit ec: ExecutionContext): DBIO[Option[T]] = {
    findOneCompiled(id).result.headOption.map(e => e.map(_postLoad))
  }

  def lock(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val result = findOneCompiled(entity.id.get).result
    result.overrideStatements(
      Seq(exclusiveLockStatement(result.statements.head))
    ).map(_ => entity)
  }

  def save(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    entity.id match {
      case None => generatedIdPersister(entity, ec)
      case Some(_) => predefinedIdPersister(entity, ec)
    }
  }

  protected val generatedIdPersister: (T, ExecutionContext) => DBIO[T] = getGeneratedIdPersister(identity)

  protected def getGeneratedIdPersister(transformer: T => T): (T, ExecutionContext) => DBIO[T] =
    (entity: T, ec: ExecutionContext) => {
      val transformed = transformer(_prePersist(entity))
      (saveCompiled += transformed).map(id => _postPersist(transformed.withId(id)))(ec)
    }

  protected val predefinedIdPersister: (T, ExecutionContext) => DBIO[T] = getPredefinedIdPersister(identity)

  protected def getPredefinedIdPersister(transformer: T => T): (T, ExecutionContext) => DBIO[T] =
    (entity: T, ec: ExecutionContext) => {
      val transformed = transformer(_prePersist(entity))
      (tableQueryCompiled += transformed).map(_ => _postPersist(transformed))(ec)
    }

  def batchInsert(entities: Seq[T]): DBIO[Option[Int]] = batchPersister(entities)

  protected val batchPersister: Seq[T] => DBIO[Option[Int]] = getBatchPersister(seq => sequenceLifecycleEvent(seq, _prePersist, classOf[prePersist]))

  protected def getBatchPersister(seqTransformer: Seq[T] => Seq[T]): Seq[T] => DBIO[Option[Int]] = {
    (entities: Seq[T]) => tableQueryCompiled ++= seqTransformer(entities)
  }

  def update(entity: T)(implicit ec: ExecutionContext): DBIO[T] = updater(entity, updateFinder(entity), ec)

  protected def updateValidator(previous: T, next: T): Int => T = _ => next

  protected def updateFinder(entity: T): F = findOneCompiled(entity.id.get)

  protected val updater: (T, F, ExecutionContext) => DBIO[T] = getUpdater(identity)

  protected def getUpdater(transformer: T => T): (T, F, ExecutionContext) => DBIO[T] =
    (entity: T, finder: F, ec: ExecutionContext) => {
      val transformed = transformer(_preUpdate(entity))
      finder.update(transformed).map(_postUpdate compose updateValidator(entity, transformed))(ec)
    }

  def delete(entity: T)(implicit ec: ExecutionContext): DBIO[T] = {
    val preDeleted = _preDelete(entity)
    findOneCompiled(entity.id.get).delete.map(_ => _postDelete(preDeleted))
  }

  def count(): DBIO[Int] = {
    countCompiled.result
  }

  def executeTransactionally[R](work: DBIO[R]): DBIO[R] = {
    work.transactionally
  }

  def exclusiveLockStatement(sql: String): String = {
    driver.getClass.getSimpleName.toLowerCase match {
      case n: String if n.contains("db2") || n.contains("derby") => sql + " FOR UPDATE WITH RS"
      case n: String if n.contains("sqlserver") => sql.replaceFirst(" where ", " WITH (UPDLOCK, ROWLOCK) WHERE ")
      case _: String => sql + " FOR UPDATE"
    }
  }

  private def sequenceLifecycleEvent(seq: Seq[T], handler: T => T, handlerType: Class[_ <: StaticAnnotation]): Seq[T] = {
    if (LifecycleHelper.isLifecycleHandlerDefined(this.getClass, handlerType)) {
      seq.map(handler)
    } else {
      seq
    }
  }

  lazy protected val tableQueryCompiled = Compiled(tableQuery)
  lazy protected val findOneCompiled = Compiled((id: Rep[Id]) => tableQuery.filter(_.id === id))
  lazy protected val saveCompiled = tableQuery returning tableQuery.map(_.id)
  lazy private val countCompiled = Compiled(tableQuery.map(_.id).length)

  private val _postLoad: (T => T) = createHandler(classOf[postLoad])
  private val _prePersist: (T => T) = createHandler(classOf[prePersist])
  private val _postPersist: (T => T) = createHandler(classOf[postPersist])
  private val _preUpdate: (T => T) = createHandler(classOf[preUpdate])
  private val _postUpdate: (T => T) = createHandler(classOf[postUpdate])
  private val _preDelete: (T => T) = createHandler(classOf[preDelete])
  private val _postDelete: (T => T) = createHandler(classOf[postDelete])

  protected def _getPrePersist: (T => T) = _prePersist

  private def createHandler(handlerType: Class[_ <: StaticAnnotation]) = {
    LifecycleHelper.createLifecycleHandler[T, Id](this, handlerType)
  }
}


