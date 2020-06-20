package com.magicube.eventflows.email

import java.net.URL

case class EmailAttachment
(
  var name: String = "",
  var description: String = "",
  var path: String = "",
  var url: URL = null,
  var disposition: String = "attachment"
) {

}

object EmailAttachment {
  val ATTACHMENT = "attachment"
  val INLINE = "inline"
}