package com.magicube.eventflows.Repository.Squeryl

import org.squeryl._

import scala.annotation.StaticAnnotation

trait IEntity[K] extends KeyedEntity[K] {
}

trait EntityBase extends IEntity[Long] {
  var id: Long
}

case class Key(autoInc: Boolean) extends StaticAnnotation