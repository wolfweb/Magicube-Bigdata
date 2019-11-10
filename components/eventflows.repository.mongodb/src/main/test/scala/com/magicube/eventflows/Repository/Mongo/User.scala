package com.magicube.eventflows.Repository.Mongo

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}
import reactivemongo.bson.Macros.Annotations.Key

case class User
(
  @Key("_id")
  var id: Int,
  var age: Int,
  var name: String,
  var scores: List[Subject]
) {}

object User {
  implicit val scoresReader: BSONDocumentReader[Subject] = Macros.reader[Subject]
  implicit val scoresWriter: BSONDocumentWriter[Subject] = Macros.writer[Subject]

  implicit val userReader: BSONDocumentReader[User] = Macros.reader[User]
  implicit val userWriter: BSONDocumentWriter[User] = Macros.writer[User]
}

case class Subject
(
  val name: String,
  val score: Int
)