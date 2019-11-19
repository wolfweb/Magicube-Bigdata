package com.magicube.eventflows.Net

import com.magicube.eventflows.Json.JSON._
import org.asynchttpclient.{AsyncHttpClient, Response}
import org.asynchttpclient.Dsl._
import org.json4s.DefaultFormats
import sun.nio.cs.UTF_8

import scala.reflect.ClassTag

case class RespMessage[T](code: Int = 0, data: T = null, message: String = "")

object Curl {
  val client: AsyncHttpClient = asyncHttpClient()

  def get(url: String): Response = client.prepareGet(url).execute().get()

  def postJson[TData: Manifest](url: String, data: TData): Response = {
    val body = serialize(data, DefaultFormats)
    client.preparePost(url)
      .setHeader("Content-Type", "application/json")
      .setBody(body)
      .execute().get()
  }
}
