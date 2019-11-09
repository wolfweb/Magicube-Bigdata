package com.magicube.eventflows

import java.util.function.Supplier

import com.sun.istack.internal.Nullable

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

  def isNull(@Nullable `object`: Any, message: String): Unit = {
    if (`object` != null) throw new IllegalArgumentException(message)
  }

  def isNull(@Nullable `object`: Any, messageSupplier: Supplier[String]): Unit = {
    if (`object` != null) throw new IllegalArgumentException(nullSafeGet(messageSupplier))
  }

  def notNull(@Nullable `object`: Any, message: String): Unit = {
    if (`object` == null) throw new IllegalArgumentException(message)
  }

  def notNull(@Nullable `object`: Any, messageSupplier: Supplier[String]): Unit = {
    if (`object` == null) throw new IllegalArgumentException(nullSafeGet(messageSupplier))
  }

  def hasLength(@Nullable text: String, message: String): Unit = {
    if (!StringUtils.hasLength(text)) throw new IllegalArgumentException(message)
  }

  @Nullable private def nullSafeGet(@Nullable messageSupplier: Supplier[String]) = if (messageSupplier != null) messageSupplier.get
  else null
}
