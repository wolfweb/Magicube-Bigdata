package com.magicube.eventflows.Repository.Squeryl

import org.squeryl._

trait IEntity[K] extends KeyedEntity[K] {
  var Id: K
}

trait EntityBase extends IEntity[Long] {}
