package com.magicube

import java.sql.Timestamp
import java.text.SimpleDateFormat

import com.magicube.eventflows.Json.JSON.deserialize
import org.asynchttpclient.Response
import org.json4s.DefaultFormats
import sun.nio.cs.UTF_8

package object eventflows {
  implicit class StringExtension(v: String) {
    def strToSqlTime(dateFormat: String) = {
      val sf = new SimpleDateFormat(dateFormat)
      new Timestamp(sf.parse(v).getTime)
    }
  }

  implicit class SqlTimeExtension(v: Timestamp) {
    def sqlTimeToString(dateFormat: String) = {
      val sf = new SimpleDateFormat(dateFormat)
      sf.format(v)
    }
  }

  implicit class CurlExtension(rep: Response) {
    def readAsString = rep.getResponseBody(UTF_8.INSTANCE)

    def readAs[T: Manifest] = deserialize[T](readAsString, DefaultFormats)
  }
}
