package com.magicube.eventflows.Repository.Squeryl

object EntityException {
  def apply(message: String): EntityException = new EntityException(message)

  def tableNotFound(name: String): EntityException = EntityException("not found table " + name)
}

class EntityException(message: String) extends RuntimeException(message)
  