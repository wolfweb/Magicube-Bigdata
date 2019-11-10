package com.magicube.eventflows

import scala.util.matching.Regex

object RegexUtil {
  def getMatchValue(regStr: String, str: String, defvalue: String) = {
    var group = 0
    val reg = if (regStr.contains("->")) {
      val arr = regStr.split("->")
      group = arr(1).toInt
      new Regex(arr(0))
    }
    else new Regex(regStr)

    val arr = reg.findAllMatchIn(str).toArray
    if (arr.size > 0)
      arr(0).group(group)
    else
      defvalue
  }
}
