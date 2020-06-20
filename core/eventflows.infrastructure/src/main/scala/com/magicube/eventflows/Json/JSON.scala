package com.magicube.eventflows.Json

import com.magicube.eventflows.Date.DateFactory.DatetimeSerializer
import com.magicube.eventflows._
import org.json4s.jackson.JsonMethods.{compact, parse, render}
import org.json4s.{Extraction, FieldSerializer, _}

object JSON {
  val thisFormat = DefaultFormats + DatetimeSerializer

  def deserialize[T: Manifest](data: String, fieldSerializer: FieldSerializer[T] = null): T = {
    implicit val format = thisFormat + (if (fieldSerializer == null) FieldSerializer[T]() else fieldSerializer)
    parse(data).extract[T]
  }

  def deserialize[T: Manifest](data: String, formats: DefaultFormats): T = {
    implicit val format = formats
    parse(data).extract[T]
  }

  def serialize[T: Manifest](data: T): String = {
    implicit var format = thisFormat

    if (!data.isPrimitive)
      format += FieldSerializer[T]()

    compact(render(Extraction.decompose(data)))
  }

  def serialize[T: Manifest](data: T, formats: DefaultFormats): String = {
    implicit val format = formats
    compact(render(Extraction.decompose(data)))
  }
}
