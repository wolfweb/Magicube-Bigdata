package com.magicube.eventflows.Repository.Squeryl

import org.squeryl._

trait EntityBase extends IEntity[Long] {}

trait IEntity[K] extends KeyedEntity[K] {}