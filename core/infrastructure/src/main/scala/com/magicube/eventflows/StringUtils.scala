package com.magicube.eventflows

import com.sun.istack.internal.Nullable

object StringUtils {
  private val FOLDER_SEPARATOR = "/"
  private val WINDOWS_FOLDER_SEPARATOR = "\\"
  private val TOP_PATH = ".."
  private val CURRENT_PATH = "."
  private val EXTENSION_SEPARATOR = '.'

  def isEmpty(@Nullable str: Any): Boolean = str == null || "" == str

  def hasLength(@Nullable str: CharSequence): Boolean = str != null && str.length > 0

  def hasLength(@Nullable str: String): Boolean = str != null && !(str.isEmpty)

  def hasText(@Nullable str: CharSequence): Boolean = str != null && str.length > 0 && containsText(str)

  def hasText(@Nullable str: String): Boolean = str != null && !(str.isEmpty) && containsText(str)

  private def containsText(str: CharSequence): Boolean = {
    val strLen = str.length()
    var i = 0
    while (i < strLen) {
      if (!Character.isWhitespace(str.charAt(i))) true
      i += 1
    }

    false
  }
}
