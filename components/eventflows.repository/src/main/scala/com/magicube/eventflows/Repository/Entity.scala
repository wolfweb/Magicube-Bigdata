package com.magicube.eventflows.Repository

abstract class Entity[T <: Entity[T, Id], Id] {
  val id: Option[Id]

  def withId(id: Id): T
}
