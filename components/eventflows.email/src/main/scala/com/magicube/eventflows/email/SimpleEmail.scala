package com.magicube.eventflows.email

import com.magicube.eventflows._

class SimpleEmail() extends EmailProvider {
  override def setMsg(msg: String): EmailProvider = {
    if (msg.isNullOrEmpty) throw new EmailException("Invalid message supplied")

    this.setContent(msg, "text/plain; charset=" + this.charset)
    this
  }
}



