package com.magicube.eventflows.Repository.Mongo

import org.slf4j.LoggerFactory
import reactivemongo.api.commands.{MultiBulkWriteResult, WriteConcern, WriteResult}
import reactivemongo.api.indexes.Index
import reactivemongo.api.{Cursor, QueryOpts, ReadPreference}
import reactivemongo.bson._
import reactivemongo.core.errors.GenericDatabaseException

import scala.concurrent._
import scala.reflect._
import scala.util._

class BulkInsertRejected extends Exception("No objects inserted. Error converting some or all to JSON")

abstract class Repository[T: ClassTag, K <: BSONValue]
(
  database: MongoDB,
  collectionName: String,
  entityReader: BSONDocumentReader[T],
  entityWriter: BSONDocumentWriter[T]
)(implicit val ec: ExecutionContext) {
  private implicit val entityReaderImplicit = entityReader
  private implicit val entityWriterImplicit = entityWriter

  protected val logger = LoggerFactory.getLogger(getClass.getName)
  protected val Id = "_id"
  protected val DuplicateKeyError = "E11000"

  val ensureIndexesRetryInterval = 10000

  val defaultReadPreference = database.connection.options.readPreference

  val collection: MongoCollection = database.collection(collectionName)

  def indexes: Seq[Index] = Seq.empty

  def find(selector: BSONDocument): Future[List[T]] = find(selector, None, BSONDocument.empty, None)

  def find(selector: BSONDocument, opt: QueryOpt): Future[List[T]] = find(selector, Some(opt), BSONDocument.empty, None)

  def find(selector: BSONDocument, opt: QueryOpt, sorting: BSONDocument): Future[List[T]] = find(selector, Some(opt), sorting, None)

  def find(selector: BSONDocument, readPreference: ReadPreference): Future[List[T]] = find(selector, None, BSONDocument.empty, Some(readPreference))

  def find(selector: BSONDocument, opt: QueryOpt, sorting: BSONDocument, readPreference: ReadPreference): Future[List[T]] = {
    find(selector, Some(opt), BSONDocument.empty, Some(readPreference))
  }

  def find(selector: BSONDocument, opt: Option[QueryOpt] = None, sorting: BSONDocument, readPreference: Option[ReadPreference]): Future[List[T]] = {
    val (skip, limit) = opt.map(opt => (opt.offset, opt.limit)).getOrElse((0, Int.MaxValue))
    val rp = readPreference.getOrElse(defaultReadPreference)

    for {
      instance <- collection.instance()
      result <- instance.find(selector, Some(BSONDocument.empty)).options(QueryOpts(skip, limit)).sort(sorting).cursor[T](rp).collect[List](limit, Cursor.FailOnError[List[T]]())
    } yield result
  }

  def findAll(opt: QueryOpt, sorting: BSONDocument): Future[List[T]] = findAll(Some(opt), sorting, None)

  def findAll(opt: QueryOpt, sorting: BSONDocument, readPreference: ReadPreference): Future[List[T]] = findAll(Some(opt), sorting, Some(readPreference))

  def findAll(opt: Option[QueryOpt] = None, sorting: BSONDocument = BSONDocument.empty, readPreference: Option[ReadPreference] = None): Future[List[T]] = {
    find(BSONDocument.empty, opt, sorting, readPreference)
  }

  def findById(id: K): Future[Option[T]] = findById(id, None)

  def findById(id: K, readPreference: Option[ReadPreference]): Future[Option[T]] = findOne($id(id), None)

  def findById(id: K, projection: Option[BSONDocument], readPreference: Option[ReadPreference] = None): Future[Option[BSONDocument]] = {
    findOne($id(id), projection, readPreference)
  }

  def findOne(query: BSONDocument, readPreference: Option[ReadPreference]): Future[Option[T]] = {
    for {
      instance <- collection.instance()
      result <- instance.find(query, Some(BSONDocument.empty)).one[T](readPreference.getOrElse(defaultReadPreference))
    } yield result
  }

  def findOne(query: BSONDocument, projection: Option[BSONDocument], readPreference: Option[ReadPreference] = None): Future[Option[BSONDocument]] = {
    for {
      instance <- collection.instance()
      result <- instance.find(query, projection).one[BSONDocument](readPreference.getOrElse(defaultReadPreference))
    } yield result
  }

  def count: Future[Int] ={
    for {
      instance <- collection.instance()
      result <- instance.count(None)
    } yield result
  }

  def count(opt: QueryOpt): Future[Int] = count(None, Some(opt))

  def count(selector: BSONDocument, opt: QueryOpt): Future[Int] = count(Some(selector), Some(opt))

  def count(selector: BSONDocument, opt: Option[QueryOpt] = None): Future[Int] = count(Some(selector), opt)

  def count(selector: Option[BSONDocument], opt: Option[QueryOpt]): Future[Int] = {
    val (skip, limit) = opt.map(opt => (opt.offset, opt.limit)).getOrElse((0, Int.MaxValue))
    for {
      instance <- collection.instance()
      result <- instance.count(selector, limit, skip)
    } yield result
  }

  def removeAll(writeConcern: WriteConcern = WriteConcern.Default): Future[OperationSuccess.type] = remove(BSONDocument.empty, writeConcern)

  def removeById(id: K, writeConcern: WriteConcern = WriteConcern.Default): Future[OperationSuccess.type] = remove($id(id), writeConcern)

  def remove(selector: BSONDocument, writeConcern: WriteConcern = WriteConcern.Default): Future[OperationSuccess.type] = {
    for {
      instance <- collection.instance()
      result <- track(instance.remove(selector, writeConcern))
    } yield result
  }

  def drop: Future[Boolean] = {
    for {
      instance <- collection.instance()
      result <- instance.drop(false)
    } yield result
  }

  def insert(entity: T, writeConcern: WriteConcern = WriteConcern.Default): Future[T] = {
    for {
      instance <- collection.instance()
      result <- track(entity, instance.insert(_: T, writeConcern))
    } yield result
  }

  def bulkInsert(entities: Traversable[T]): Future[MultiBulkWriteResult] ={
    for {
      instance <- collection.instance()
      result <- instance.insert(true).many(entities.map(entityWriter.write(_)).toIterable)
    } yield result
  }

  def updateById(id: K, entity: T): Future[Option[T]] = updateBy($id(id), entity)

  def updateById(id: K, ops: UpdateOperation*): Future[Option[T]] = updateBy($id(id), ops.map(_.toDocument).foldLeft(BSONDocument.empty)(_ ++ _))

  def updateBy(selector: BSONDocument, entity: T): Future[Option[T]] = updateBy(selector, entityWriter.write(entity))

  def updateBy(selector: BSONDocument, op: UpdateOperation): Future[Option[T]] = updateBy(selector, op.toDocument)

  def updateById(id: K, modifier: BSONDocument): Future[Option[T]] = updateBy($id(id), modifier)

  def updateBy(selector: BSONDocument, modifier: BSONDocument): Future[Option[T]] =
    for {
      instance <- collection.instance()
      result <- instance.findAndModify(selector, instance.updateModifier(modifier, true)).map(_.value)
    } yield result.map(_.as[T])

  private def ensureIndex(index: Index): Future[OperationSuccess.type] ={
    for {
      instance <- collection.instance()
      result <- track(instance.indexesManager.create(index))
    } yield result
  }

  def ensureIndexes: Future[Boolean] = {
    for {
      instance <- collection.instance()
      pendingIdxs <- instance.indexesManager(ec).list().map(idxs => {
        val currentIdx = idxs.map(_.name).flatten
        val pendingIdxs = indexes.filter { newIdx => !currentIdx.contains(newIdx.name.get) }
        logger(s"Current idxs: ${currentIdx.mkString(", ")}")
        logger(s"Pending idxs: ${pendingIdxs.map(idx => idx.name.getOrElse(idx)).mkString(", ")}")
        pendingIdxs
      })
      insertedIdx <- Future.sequence {
        pendingIdxs.map(idx =>
          ensureIndex(idx).andThen {
            case Success(result) =>
              logger(s"Index ${idx.name.getOrElse(idx)} inserted")
            case Failure(ex) =>
              logger(s"Index ${idx.name.getOrElse(idx)} fail", ex)
          })
      }
    } yield insertedIdx.forall(_ == OperationSuccess)
  }

  private def $id(id: K) = BSONDocument(Id -> id)

  private def track[T](entity: T, cmd: T => Future[WriteResult])(implicit ec: ExecutionContext): Future[T] =
    cmd(entity).recoverWith {
      case e => Future.failed(new RuntimeException(s"Got exception from MongoDB: ${e.getMessage}", e.getCause))
    }.flatMap {
      case wr if wr.ok =>
        Future.successful(entity)
      case wr =>
        Future.failed(GenericDatabaseException(wr.writeErrors.map(_.errmsg).mkString(", "), wr.code))
    }

  private def track(cmd: Future[WriteResult])(implicit ec: ExecutionContext): Future[OperationSuccess.type] =
    cmd.recoverWith {
      case e => Future.failed(new RuntimeException(s"Got exception from MongoDB: ${e.getMessage}", e.getCause))
    }.flatMap {
      case wr if wr.ok =>
        Future.successful(OperationSuccess)
      case wr =>
        Future.failed(GenericDatabaseException(wr.writeErrors.map(_.errmsg).mkString(", "), wr.code))
    }
}