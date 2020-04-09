package com.magicube.eventflows

import java.util.function.Supplier

object Assert {
  def state(expression: Boolean, message: String): Unit = {
    if (!expression) throw new IllegalStateException(message)
  }

  def state(expression: Boolean, messageSupplier: Supplier[String]): Unit = {
    if (!expression) throw new IllegalStateException(nullSafeGet(messageSupplier))
  }

  def isTrue(expression: Boolean, message: String): Unit = {
    if (!expression) throw new IllegalArgumentException(message)
  }

  def isTrue(expression: Boolean, messageSupplier: Supplier[String]): Unit = {
    if (!expression) throw new IllegalArgumentException(nullSafeGet(messageSupplier))
  }

  def isNull(`object`: Any, message: String): Unit = {
    if (`object` != null) throw new IllegalArgumentException(message)
  }

  def isNull(`object`: Any, messageSupplier: Supplier[String]): Unit = {
    if (`object` != null) throw new IllegalArgumentException(nullSafeGet(messageSupplier))
  }

  def notNull(`object`: Any, message: String): Unit = {
    if (`object` == null) throw new IllegalArgumentException(message)
  }

  def notNull(`object`: Any, messageSupplier: Supplier[String]): Unit = {
    if (`object` == null) throw new IllegalArgumentException(nullSafeGet(messageSupplier))
  }

  private def nullSafeGet(messageSupplier: Supplier[String]) = if (messageSupplier != null) messageSupplier.get else null
}
