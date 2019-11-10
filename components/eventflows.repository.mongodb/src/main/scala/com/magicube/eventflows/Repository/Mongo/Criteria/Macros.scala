package com.magicube.eventflows.Repository.Mongo.Criteria

import scala.reflect.macros.blackbox.Context

object TypedMacros {

  import Typed.PropertyAccess

  def createTerm[T <: AnyRef: c.WeakTypeTag, U: c.WeakTypeTag](c: Context { type PrefixType = PropertyAccess[T] })(statement: c.Tree): c.Tree =
  {
    import c.universe._

    val q"""(..$args) => $select""" = statement

    val selectors = select.collect {
      case Select(_, TermName(property)) => property
    }.reverse.mkString(".")

    val propertyType = weakTypeOf[U]

    q"""new Term[${propertyType}] (${selectors})"""
  }
}

