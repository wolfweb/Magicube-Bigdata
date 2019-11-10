package com.magicube.eventflows.Repository.Mongo.Criteria

import reactivemongo.bson._

final case class Expression(name: Option[String], element: BSONElement) {
  def unary_! : Expression =
    this match {
      case Expression(Some(term), BSONElement("$in", vals)) => Expression(term, ("$nin", vals))

      case Expression(Some(term), BSONElement("$nin", vals)) => Expression(term, ("$in", vals))

      case Expression(Some(term), BSONElement("$ne", vals)) => Expression(term, (term, vals))

      case Expression(Some(term), BSONElement("$exists", BSONBoolean(value))) => Expression(Some(term), ("$exists" -> !value))

      case Expression(Some(term), BSONElement(field, vals)) if (field == term) => Expression(term, ("$ne", vals))

      case Expression(None, BSONElement("$nor", vals)) => Expression(None, ("$or" -> vals))

      case Expression(None, BSONElement("$or", vals)) => Expression(None, ("$nor" -> vals))

      case Expression(Some("$not"), el) => Expression(None, el)

      case Expression(Some(n), _) => Expression(Some("$not"), (n -> BSONDocument(element)))

      case Expression(None, el) => Expression(Some("$not"), el)
    }

  def &&(rhs: Expression): Expression = combine("$and", rhs)

  def !&&(rhs: Expression): Expression = combine("$nor", rhs)

  def ||(rhs: Expression): Expression = combine("$or", rhs)

  def isEmpty: Boolean = name.isEmpty && element.name.isEmpty

  private def combine(op: String, rhs: Expression): Expression =
    if (rhs.isEmpty)
      this
    else
      element match {
        case BSONElement(`op`, arr: BSONArray) => Expression(None, (op, arr ++ BSONArray(toBSONDocument(rhs))))
        case BSONElement("", _) => rhs
        case _ => Expression(None, op -> BSONArray(toBSONDocument(this), toBSONDocument(rhs)))
      }
}

object Expression {
  val empty = new Expression(None, "" -> BSONDocument.empty)

  def apply(name: String, element: BSONElement): Expression = new Expression(Some(name), element)
}
