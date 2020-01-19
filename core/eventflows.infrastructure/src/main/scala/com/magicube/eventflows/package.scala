package com.magicube

import java.nio.charset.Charset
import java.sql.Timestamp
import java.text.SimpleDateFormat

import com.magicube.eventflows.Json.JSON.deserialize
import org.asynchttpclient.Response
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json4s.DefaultFormats

import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._
import scala.collection.mutable.ListBuffer

import scala.util.matching.Regex

package object eventflows {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss"

  implicit def toDateTime(v: String) = {
    val pattern = extractDatePattern(v)
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

  def getAnnotations[T <: StaticAnnotation](tpe: Type, cls: Class[_]): List[T] = {
    val mirror = runtimeMirror(cls.getClassLoader)
    val annotations = tpe.typeSymbol.annotations

    val res = ListBuffer[T]()
    for (annt <- annotations) {
      val anntCls = annt.tree.tpe.typeSymbol.asClass
      val classMirror = mirror.reflectClass(anntCls);
      val anntType = annt.tree.tpe
      val constructor = anntType.decl(termNames.CONSTRUCTOR).asMethod;
      val constructorMirror = classMirror.reflectConstructor(constructor);

      val instance = annt.tree match {
        case Apply(c, args: List[Tree]) =>
          val res = args.collect({
            case i: Tree =>
              i match {
                case Literal(Constant(value)) =>
                  value
              }
          })
          constructorMirror(res: _*).asInstanceOf[T]
      }


      res += (instance)
    }
    res.toList
  }

  private def extractDatePattern(v: String): String = {
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
