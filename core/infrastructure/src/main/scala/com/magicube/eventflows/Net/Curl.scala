package com.magicube.eventflows.Net

import java.nio.charset.{Charset, StandardCharsets}

import com.m3.curly.scala.{HTTP, Request}
import org.json4s.DefaultFormats
import com.magicube.eventflows.Json.JSON._

case class RespMessage[T](code: Int = 0, data: T = null, message: String = "")

object Curl {
  def getAsString(url: String): String = {
    HTTP.get(url).asString()
  }

  def get[T: Manifest](url: String): RespMessage[T] = {
    val result = HTTP.get(url).asString()
    deserialize[RespMessage[T]](result, DefaultFormats)
  }

  def get[T: Manifest](req: Request): RespMessage[T] = {
    val result = HTTP.get(req).asString()
    deserialize[RespMessage[T]](result, DefaultFormats)
  }

  def postAsJson[T: Manifest, TData: Manifest](url: String, data: TData): RespMessage[T] = {
    val result = HTTP.post(url, serialize(data, DefaultFormats)).asString()
    deserialize[RespMessage[T]](result, DefaultFormats)
  }

  def postAsJson[T: Manifest, TData: Manifest](req: Request, data: TData): RespMessage[T] = {
    req.body(serialize(data, DefaultFormats).getBytes(Charset.forName("UTF-8")), "application/json")
    val result = HTTP.post(req).asString()
    deserialize[RespMessage[T]](result, DefaultFormats)
  }

  def post[T: Manifest](req: Request, data: String): String = {
    req.body(data.getBytes(StandardCharsets.UTF_8))
    HTTP.post(req).asString()
  }
}
