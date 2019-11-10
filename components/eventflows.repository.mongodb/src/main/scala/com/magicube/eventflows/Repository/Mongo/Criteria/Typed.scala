package com.magicube.eventflows.Repository.Mongo.Criteria

import scala.language.experimental.macros

object Typed {
  final class PropertyAccess[ParentT <: AnyRef] {
    def apply[T](statement: ParentT => T): Term[T] = macro TypedMacros.createTerm[ParentT, T]
  }

  def criteria[T <: AnyRef]: PropertyAccess[T] = new PropertyAccess[T]
}
