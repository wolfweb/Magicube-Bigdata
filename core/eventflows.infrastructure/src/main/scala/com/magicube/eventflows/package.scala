package com.magicube

import java.nio.charset.Charset
import java.sql.Timestamp
import java.text.SimpleDateFormat

import com.magicube.eventflows.Json.JSON.deserialize
import org.asynchttpclient.Response
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s.DefaultFormats

import scala.util.matching.Regex

package object eventflows {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"

  implicit def toDateTime(v: String) = {
    val pattern = extractDataPattern(v)
    DateTime.parse(v, DateTimeFormat.forPattern(pattern))
  }

  implicit def toDateTime(v: Timestamp) = {
    val sf = new SimpleDateFormat(dateFormat)
    DateTime.parse(sf.format(v), DateTimeFormat.forPattern(dateFormat))
  }

  implicit def toSqlTime(v: DateTime) = {
    val sf = new SimpleDateFormat(dateFormat)
    new Timestamp(sf.parse(v.toString(dateFormat)).getTime)
  }

  implicit class SqlTimeExtension(v: Timestamp) {
    def toString(dateFormat: String) = {
      val sf = new SimpleDateFormat(dateFormat)
      sf.format(v)
    }
  }

  implicit class CurlExtension(rep: Response) {
    def readAsString = rep.getResponseBody(Charset.forName("UTF-8"))

    def readAs[T: Manifest] = deserialize[T](readAsString, DefaultFormats)
  }

  private def extractDataPattern(v: String): String = {
    val reg = new Regex("(\\d+)([/\\-]+)(\\d+)([/\\-]+)(\\d+)([T\\s]+)(\\d+)(:)(\\d+)(:)(\\d+)([\\.\\d+]*)(Z*)")
    val m = reg.findFirstMatchIn(v)
    val groups = m.get.subgroups.toList
    val builder = new StringBuilder
    builder ++= groups(0).flatMap(x => "y")
    builder ++= groups(1)
    builder ++= groups(2).flatMap(x => "M")
    builder ++= groups(3)
    builder ++= groups(4).flatMap(x => "d")
    val flag = if (groups(5) == "T") "'T'" else groups(5)
    builder ++= flag
    builder ++= groups(6).flatMap(x => "H")
    builder ++= groups(7)
    builder ++= groups(8).flatMap(x => "m")
    builder ++= groups(9)
    builder ++= groups(10).flatMap(x => "s")
    if (groups.size > 10) {
      val suffix = if (groups(11).startsWith(".")) groups(11).flatMap(x => {
        x match {
          case '.' => "."
          case _ => "S"
        }
      }).mkString else ""
      if (suffix != "") {
        builder ++= suffix
        builder ++= groups(12)
      } else {
        builder ++= groups(12)
      }
    }
    builder.mkString
  }
}
