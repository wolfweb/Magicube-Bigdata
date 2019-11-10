package com.magicube.eventflows.Repository

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, FailoverStrategy, MongoConnection, MongoDriver}
import reactivemongo.bson._

import scala.concurrent._
import scala.util._

package object Mongo {

  case class MissingDatabaseName(uri: String) extends RuntimeException(s"Missing database name in uri $uri")

  case class QueryOpt(offset: Int, limit: Int)

  case class MongoDB(name: String, connection: MongoConnection, instance: () => Future[DefaultDB]) {
    def collection(collName: String)(implicit ec: ExecutionContext): MongoCollection =
      MongoCollection(collName, this, () => instance().map(_.collection(collName)))
  }

  case class MongoCollection(name: String, database: MongoDB, instance: () => Future[BSONCollection])

  object MongoConnector {
    def apply(mongoUri: String, failoverStrategy: Option[FailoverStrategy] = None)(implicit ec: ExecutionContext): Try[MongoDB] = {
      val driver = new MongoDriver()
      for {
        uri <- MongoConnection.parseURI(mongoUri)
        connection = driver.connection(uri, true)
        dbName <- uri.db.map(Success(_)).getOrElse(Failure(MissingDatabaseName(mongoUri)))
      } yield {
        connection match {
          case Success(conn) => {
            val instance = failoverStrategy.map(fos => () => conn.database(dbName, fos)).getOrElse(() => conn.database(dbName))
            MongoDB(dbName, conn, instance)
          }
          case Failure(e) => null
        }
      }
    }
  }

  case object OperationSuccess

  implicit def toBSONValue[T](value: T)(implicit w: BSONWriter[T, _ <: BSONValue]): BSONValue = w.write(value)

  implicit def MapBSONReader[T](implicit reader: BSONReader[_ <: BSONValue, T]): BSONDocumentReader[Map[String, T]] =
    new BSONDocumentReader[Map[String, T]] {
      def read(doc: BSONDocument): Map[String, T] =
        doc.elements
          .collect {
            case BSONElement(key, value) =>
              value.seeAsOpt[T](reader) map { ov =>
                (key, ov)
              }
          }
          .flatten
          .toMap
    }

  implicit def MapBSONWriter[T](implicit writer: BSONWriter[T, _ <: BSONValue]): BSONDocumentWriter[Map[String, T]] =
    new BSONDocumentWriter[Map[String, T]] {
      def write(doc: Map[String, T]): BSONDocument =
        BSONDocument(doc.toTraversable map (t => (t._1, writer.write(t._2))))
    }

}
