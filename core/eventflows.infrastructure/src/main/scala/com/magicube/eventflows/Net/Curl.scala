package com.magicube.eventflows.Net

import com.magicube.eventflows._
import com.magicube.eventflows.Json.JSON._
import org.asynchttpclient.Dsl._
import org.asynchttpclient.{AsyncHttpClient, Request, Response}
import org.json4s.DefaultFormats

case class RespMessage[T](code: Int = 0, data: T = null, message: String = "")

object Curl {
  val client: AsyncHttpClient = asyncHttpClient()

  def get(url: String): Response = client.prepareGet(url).execute().get()

  def post(url: String, data: Map[String, String]): Response = {
    post(url, "application/x-www-form-urlencoded", data.map(x => s"${x._1}=${x._2.urlEncode}").mkString("&"))
  }

  def post(url: String, contextType: String, data: String): Response = {
    client.preparePost(url)
      .setHeader("Content-Type", contextType)
      .setBody(data)
      .execute().get()
  }

  def postJson[TData: Manifest](url: String, data: TData): Response = {
    val body = serialize(data, DefaultFormats)
    client.preparePost(url)
      .setHeader("Content-Type", "application/json")
      .setBody(body)
      .execute().get()
  }

  def postJson[TData: Manifest](req: Request): Response = {
    client.executeRequest(req).get()
  }
}
