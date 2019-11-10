package com.magicube.eventflows.Repository.Mongo.Criteria

import reactivemongo.bson._

import scala.language.dynamics

final case class Term[T](`_term$name`: String) extends Dynamic {
  def ===[U <: T : ValueBuilder](rhs: U): Expression = Expression(
    `_term$name`,
    `_term$name` -> implicitly[ValueBuilder[U]].bson(rhs)
  )

  def @==[U <: T : ValueBuilder](rhs: U): Expression = ===[U](rhs)

  def <>[U <: T : ValueBuilder](rhs: U): Expression = Expression(
    `_term$name`,
    "$ne" -> implicitly[ValueBuilder[U]].bson(rhs)
  )

  def =/=[U <: T : ValueBuilder](rhs: U): Expression = <>[U](rhs)

  def !==[U <: T : ValueBuilder](rhs: U): Expression = <>[U](rhs)

  def <[U <: T : ValueBuilder](rhs: U): Expression = Expression(
    `_term$name`,
    "$lt" -> implicitly[ValueBuilder[U]].bson(rhs)
  )

  def <=[U <: T : ValueBuilder](rhs: U): Expression = Expression(
    `_term$name`,
    "$lte" -> implicitly[ValueBuilder[U]].bson(rhs)
  )

  def >[U <: T : ValueBuilder](rhs: U): Expression = Expression(
    `_term$name`,
    "$gt" -> implicitly[ValueBuilder[U]].bson(rhs)
  )

  def >=[U <: T : ValueBuilder](rhs: U): Expression = Expression(
    `_term$name`,
    "$gte" -> implicitly[ValueBuilder[U]].bson(rhs)
  )

  def exists: Expression = Expression(
    `_term$name`,
    "$exists" -> BSONBoolean(true)
  )

  def in[U <: T : ValueBuilder](values: Traversable[U])(implicit B: ValueBuilder[U]): Expression = Expression(
    `_term$name`,
    "$in" -> BSONArray(values map (B.bson))
  )

  def in[U <: T : ValueBuilder](head: U, tail: U*)(implicit B: ValueBuilder[U]): Expression = Expression(
    `_term$name`,
    "$in" -> BSONArray(Seq(B.bson(head)) ++ tail.map(B.bson))
  )

  def selectDynamic[U](field: String): Term[U] = Term[U](`_term$name` + "." + field)
}

object Term {

  implicit class CollectionTermOps[F[_] <: Traversable[_], T](val term: Term[F[T]]) extends AnyVal {
    def all(values: Traversable[T])(implicit B: ValueBuilder[T]): Expression = Expression(
      term.`_term$name`,
      "$all" -> BSONArray(values map (B.bson))
    )

    def elemMatch(exp: Expression): Expression = Expression(
      term.`_term$name`,
      "$elemMatch" -> toBSONDocument(exp)
    )
  }

  implicit class StringTermOps[T >: String](val term: Term[T]) extends AnyVal {
    def =~(re: (String, RegexModifier)): Expression = Expression(
      term.`_term$name`,
      "$regex" -> BSONRegex(re._1, re._2.value)
    )

    def =~(re: String): Expression = Expression(
      term.`_term$name`,
      "$regex" -> BSONRegex(re, "")
    )

    def !~(re: (String, RegexModifier)): Expression = Expression(
      term.`_term$name`,
      "$not" -> BSONDocument("$regex" -> BSONRegex(re._1, re._2.value))
    )

    def !~(re: String): Expression = Expression(
      term.`_term$name`,
      "$not" -> BSONDocument("$regex" -> BSONRegex(re, ""))
    )
  }
}

sealed trait RegexModifier {
  def |(other: RegexModifier): RegexModifier = CombinedRegexModifier(this, other)

  def value(): String
}

case class CombinedRegexModifier
(
  lhs: RegexModifier,
  rhs: RegexModifier
) extends RegexModifier {
  override def value(): String = lhs.value + rhs.value
}

case object DotMatchesEverything extends RegexModifier {
  override val value: String = "s"
}

case object ExtendedExpressions extends RegexModifier {
  override val value: String = "x"
}

case object IgnoreCase extends RegexModifier {
  override val value: String = "i"
}

case object MultilineMatching extends RegexModifier {
  override val value: String = "m"
}
