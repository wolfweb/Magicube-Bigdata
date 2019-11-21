package com.magicube

import java.nio.charset.Charset
import java.sql.Timestamp
import java.text.SimpleDateFormat

import com.magicube.eventflows.Json.JSON.deserialize
import org.asynchttpclient.Response
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s.DefaultFormats

package object eventflows {

  implicit class StringExtension(v: String) {
    def toSqlTime(dateFormat: String) = {
      val sf = new SimpleDateFormat(dateFormat)
      new Timestamp(sf.parse(v).getTime)
    }

    def toDateTime(dateFormat: String) = {
      DateTime.parse(v, DateTimeFormat.forPattern(dateFormat))
    }
  }

  implicit class SqlTimeExtension(v: Timestamp) {
    def toString(dateFormat: String) = {
      val sf = new SimpleDateFormat(dateFormat)
      sf.format(v)
    }

    def toDateTime(dateFormat: String) = {
      val str = toString(dateFormat)
      str.toDateTime(dateFormat)
    }
  }

  implicit class CurlExtension(rep: Response) {
    def readAsString = rep.getResponseBody(Charset.forName("UTF-8"))

    def readAs[T: Manifest] = deserialize[T](readAsString, DefaultFormats)
  }

}
