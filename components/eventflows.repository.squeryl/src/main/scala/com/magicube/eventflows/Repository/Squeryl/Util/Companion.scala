package com.magicube.eventflows.Repository.Squeryl.Util

import org.slf4j.LoggerFactory

object Companion {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  def of[T: Manifest]: Option[AnyRef] = try {
    val classOfT = implicitly[Manifest[T]].runtimeClass
    val companionClassName = classOfT.getName + "$"
    val companionClass = Class.forName(companionClassName)
    val moduleField = companionClass.getField("MODULE$")
    Some(moduleField.get(null))
  } catch {
    case e: Throwable => {
      logger.error(s"$e ${e.getCause}")
      None
    }
  }
}
