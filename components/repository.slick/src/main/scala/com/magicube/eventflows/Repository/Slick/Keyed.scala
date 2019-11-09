package com.magicube.eventflows.Repository.Slick

import slick.lifted.Rep

trait Keyed[Id] {
  def id: Rep[Id]
}
