package com.magicube

import java.net.{URLDecoder, URLEncoder}
import java.nio.charset.Charset
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.magicube.eventflows.Json.JSON.deserialize
import org.asynchttpclient.Response
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.matching.Regex

package object eventflows {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"

  implicit def dateTime2Timestamp(v: DateTime): Long = v.getMillis

  implicit def toDateTime(v: Long): DateTime = new DateTime(v)

  implicit def toDateTime(v: String): DateTime = {
    val pattern = extractDatePattern(v)
    DateTime.parse(v, DateTimeFormat.forPattern(pattern))
  }

  implicit def dateTimeToString(v: DateTime): String = {
    val dateTime = new DateTime(v, DateTimeZone.UTC)
    dateTime.toString(dateFormat)
  }

  implicit def toDateTime(v: Timestamp): DateTime = {
    val sf = new SimpleDateFormat(dateFormat)
    DateTime.parse(sf.format(v), DateTimeFormat.forPattern(dateFormat))
  }

  implicit def toTimestamp(v: DateTime): Timestamp = {
    val sf = new SimpleDateFormat(dateFormat)
    new Timestamp(sf.parse(v.toString(dateFormat)).getTime)
  }

  implicit def timeStampToString(v: Timestamp): String = {
    val sf = new SimpleDateFormat(dateFormat)
    sf.setTimeZone(TimeZone.getTimeZone("GMT"))
    sf.format(v)
  }

  implicit class CurlExtension(rep: Response) {
    def readAsString = rep.getResponseBody(Charset.forName("UTF-8"))

    def readStream = rep.getResponseBodyAsStream

    def readAsBytes = rep.getResponseBodyAsBytes

    def readAs[T: Manifest] = deserialize[T](readAsString)
  }

  implicit class ReflectorExtension(ref: AnyRef) {
    def getV(name: String): Any = ref.getClass.getMethods.find(_.getName == name).get.invoke(ref)

    def setV(name: String, value: Any): Unit = {
      val cls = ref.getClass
      val method = cls.getMethods.find(_.getName == name + "_$eq")
      method match {
        case Some(x) => x.invoke(ref, value.asInstanceOf[AnyRef])
        case None => throw new IllegalArgumentException(s"${cls.getName}'s [$name] field no setter")
      }
    }
  }

  implicit class ReflectorAnyExtension(v: Any) {
    private val wrapperTypes = List[Class[_]](
      classOf[Integer],
      classOf[java.lang.Byte],
      classOf[java.lang.Boolean],
      classOf[java.lang.Short],
      classOf[java.lang.Long],
      classOf[java.lang.Float],
      classOf[java.lang.Double],
      classOf[Int],
      classOf[Byte],
      classOf[Boolean],
      classOf[Short],
      classOf[Long],
      classOf[Float],
      classOf[Double],
      classOf[String]
    )

    def isPrimitive: Boolean = wrapperTypes.contains(v.getClass)
  }

  implicit class StringExtension(str: String) {
    def isNullOrEmpty: Boolean = str == null || "" == str

    def trim(c: String): String = {
      str.stripPrefix(c).stripSuffix(c)
    }

    def urlDecode = {
      val res = """\\u([0-9a-fA-F]{4})""".r.replaceAllIn(str, m => Integer.parseInt(m.group(1), 16).toChar.toString)
      URLDecoder.decode(res, "UTF-8")
    }

    def urlEncode = {
      URLEncoder.encode(str, "UTF-8")
    }
  }

  def timer[A](name: String)(block: => A) = {
    val begin = System.nanoTime
    val result = block
    val end = System.nanoTime
    val delta = end - begin
    println(s"exec $name used ${delta / 1000000d}s")
    result
  }

  private def extractDatePattern(v: String): String = {
    val reg = new Regex("(\\d+)([/\\-]+)(\\d+)([/\\-]+)(\\d+)([T\\s]*)(\\d*)(:*)(\\d*)(:*)(\\d*)([\\.\\d+]*)(Z*)")
    val m = reg.findFirstMatchIn(v)
    val groups = m.get.subgroups
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
