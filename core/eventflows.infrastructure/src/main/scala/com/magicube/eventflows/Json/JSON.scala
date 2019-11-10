package com.magicube.eventflows.Json

import com.magicube.eventflows.Date.DateFactory.DatetimeSerializer

import org.json4s.jackson.JsonMethods.{compact, parse, render}
import org.json4s.{Extraction, Formats}

object JSON {
  def deserialize[T: Manifest](data: String, formats: Formats): T = {
    implicit val thisFormat = formats + DatetimeSerializer
    parse(data).extract[T]
  }

  def serialize[T: Manifest](data: T, formats: Formats): String = {
    implicit val thisFormat = formats + DatetimeSerializer
    compact(render(Extraction.decompose(data)))
  }
}
