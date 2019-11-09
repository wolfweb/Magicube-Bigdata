package com.magicube.eventflows.Sql

import com.magicube.eventflows.Json.JSON._
import org.json4s.Formats

import scala.util.matching.Regex

object SqlHelper {
  def sqlFormatParams[T: Manifest](data: T, formats: Formats): String = {
    val str = serialize(data, formats)
    str.replaceAll("]|\\[", "")
  }

  def matchIn(txt: String, r: Regex): String = {
    val groups = r.findAllMatchIn(txt).map(x => x.subgroups.head).toList
    var result = ""
    if (groups.nonEmpty) {
      result = groups.head
    }
    result
  }

  def decodeSpecialChars(content: String): String = {
    var afterDecode = content.replaceAll("'", "''")
    afterDecode = afterDecode.replaceAll("\\\\", "\\\\\\\\")
    afterDecode = afterDecode.replaceAll("%", "\\\\%")
    afterDecode = afterDecode.replaceAll("_", "\\\\_")
    afterDecode
  }
}
