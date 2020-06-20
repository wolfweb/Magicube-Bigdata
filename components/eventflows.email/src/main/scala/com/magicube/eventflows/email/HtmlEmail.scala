package com.magicube.eventflows.email

import java.io.{File, IOException, InputStream}
import java.net.{MalformedURLException, URL}
import java.util
import java.util.UUID

import com.magicube.eventflows._
import javax.activation.{DataHandler, DataSource, FileDataSource, URLDataSource}
import javax.mail.internet.{MimeBodyPart, MimeMultipart}

import scala.collection.JavaConverters._

class HtmlEmail() extends MultiPartEmail {
  protected var text: String = null
  protected var html: String = null
  protected var inlineEmbeds = new util.HashMap[String, InlineImage]()

  def setTextMsg(aText: String): HtmlEmail = {
    if (aText.isNullOrEmpty) throw new EmailException("Invalid message supplied")

    this.text = aText
    this
  }

  def setHtmlMsg(aHtml: String): HtmlEmail = {
    if (aHtml.isNullOrEmpty) throw new EmailException("Invalid message supplied")

    this.html = aHtml
    this
  }

  override def setMsg(msg: String): EmailProvider = {
    if (msg.isNullOrEmpty) throw new EmailException("Invalid message supplied")

    this.setTextMsg(msg)
    val htmlMsgBuf = new StringBuffer(msg.length + HtmlEmail.HTML_MESSAGE_START.length + HtmlEmail.HTML_MESSAGE_END.length)
    htmlMsgBuf.append(HtmlEmail.HTML_MESSAGE_START).append(msg).append(HtmlEmail.HTML_MESSAGE_END)
    this.setHtmlMsg(htmlMsgBuf.toString)
    this
  }

  def embed(urlString: String, name: String): String = {
    try {
      this.embed(new URL(urlString), name)
    } catch {
      case e: MalformedURLException => throw new EmailException("Invalid URL", e)
    }
  }

  def embed(url: URL, name: String): String = {
    if (name.isNullOrEmpty) throw new EmailException("name cannot be null or empty")

    if (this.inlineEmbeds.containsKey(name)) {
      val ii = this.inlineEmbeds.get(name)
      val urlDataSource = ii.getDataSource.asInstanceOf[URLDataSource]
      if (url.toExternalForm.equals(urlDataSource.getURL.toExternalForm))
        ii.getCid
      else
        throw new EmailException("embedded name '" + name + "' is already bound to URL " + urlDataSource.getURL + "; existing names cannot be rebound")
    }
    else {
      var is: InputStream = null
      try {
        is = url.openStream
      } catch {
        case e: IOException => throw new EmailException("Invalid URL", e)
      } finally {
        try {
          if (is != null) is.close
        } catch {
          case e: IOException => e.printStackTrace()
        }
      }
    }
    this.embed(new URLDataSource(url), name)
  }

  def embed(file: File): String = {
    val cid = UUID.randomUUID().toString
    this.embed(file, cid)
  }

  def embed(file: File, cid: String): String = {
    if (file.getName.isNullOrEmpty) throw new EmailException("file name cannot be null or empty")

    var filePath: String = null
    try {
      filePath = file.getCanonicalPath
    } catch {
      case e: IOException => throw new EmailException("couldn't get canonical path for " + file.getName, e)
    }
    if (this.inlineEmbeds.containsKey(file.getName)) {
      val ii = this.inlineEmbeds.get(file.getName)
      val fileDataSource = ii.getDataSource.asInstanceOf[FileDataSource]
      var existingFilePath: String = null
      try {
        existingFilePath = fileDataSource.getFile.getCanonicalPath
      } catch {
        case e: IOException => throw new EmailException("couldn't get canonical path for file " + fileDataSource.getFile.getName + "which has already been embedded", e)
      }
      if (filePath == existingFilePath) ii.getCid
      else
        throw new EmailException("embedded name '" + file.getName + "' is already bound to file " + existingFilePath + "; existing names cannot be rebound")
    }
    else if (!file.exists) throw new EmailException("file " + filePath + " doesn't exist")
    else if (!file.isFile) throw new EmailException("file " + filePath + " isn't a normal file")
    else if (!file.canRead) throw new EmailException("file " + filePath + " isn't readable")
    else this.embed(new FileDataSource(file), file.getName, cid)
  }

  def embed(dataSource: DataSource, name: String): String = {
    if (this.inlineEmbeds.containsKey(name)) {
      val ii = this.inlineEmbeds.get(name)
      if (dataSource.equals(ii.getDataSource)) ii.getCid
      else throw new EmailException("embedded DataSource '" + name + "' is already bound to name " + ii.getDataSource.toString + "; existing names cannot be rebound")
    }
    else {
      val cid = UUID.randomUUID().toString
      this.embed(dataSource, name, cid)
    }
  }

  def embed(dataSource: DataSource, name: String, cid: String): String = {
    if (name.isNullOrEmpty) throw new EmailException("name cannot be null or empty")

    val mbp = new MimeBodyPart
    try {
      val encodedCid = cid.urlEncode
      mbp.setDataHandler(new DataHandler(dataSource))
      mbp.setFileName(name)
      mbp.setDisposition("inline")
      mbp.setContentID("<" + encodedCid + ">")
      val ii = new InlineImage(encodedCid, dataSource, mbp)
      this.inlineEmbeds.put(name, ii)
      encodedCid
    } catch {
      case e => throw e
    }
  }

  override def buildMimeMessage(): Unit = {
    try
      this.build()
    catch {
      case e => throw e
    }
    super.buildMimeMessage()
  }

  private def build(): Unit = {
    val rootContainer = this.getContainer
    var bodyEmbedsContainer = rootContainer
    var bodyContainer = rootContainer
    var msgHtml: MimeBodyPart = null
    var msgText: MimeBodyPart = null
    rootContainer.setSubType("mixed")
    if (!this.html.isNullOrEmpty && this.inlineEmbeds.size > 0) {
      bodyEmbedsContainer = new MimeMultipart("related")
      bodyContainer = bodyEmbedsContainer
      this.addPart(bodyEmbedsContainer, 0)
      if (!this.text.isNullOrEmpty) {
        bodyContainer = new MimeMultipart("alternative")
        val bodyPart = this.createBodyPart
        try {
          bodyPart.setContent(bodyContainer)
          bodyEmbedsContainer.addBodyPart(bodyPart, 0)
        } catch {
          case e => throw e
        }
      }
    }
    else if (!this.text.isNullOrEmpty && !this.html.isNullOrEmpty)
      if (this.inlineEmbeds.size <= 0 && !this.isBoolHasAttachments)
        rootContainer.setSubType("alternative")
      else {
        bodyContainer = new MimeMultipart("alternative")
        this.addPart(bodyContainer, 0)
      }
    if (!this.html.isNullOrEmpty) {
      msgHtml = new MimeBodyPart()
      bodyContainer.addBodyPart(msgHtml, 0)
      msgHtml.setText(this.html, this.charset, "html")
      val contentType = msgHtml.getContentType
      if (contentType == null || !(contentType == "text/html"))
        if (!this.charset.isNullOrEmpty)
          msgHtml.setContent(this.html, "text/html; charset=" + this.charset)
        else
          msgHtml.setContent(this.html, "text/html")

      for ((k, v) <- this.inlineEmbeds.asScala) {
        bodyEmbedsContainer.addBodyPart(v.getMbp)
      }
    }
    if (!this.text.isNullOrEmpty) {
      msgText = new MimeBodyPart
      bodyContainer.addBodyPart(msgText, 0)
      msgText.setText(this.text, this.charset)
    }
  }
}

object HtmlEmail {
  val CID_LENGTH = 10
  private val HTML_MESSAGE_START = "<html><body><pre>"
  private val HTML_MESSAGE_END = "</pre></body></html>"
}

class InlineImage(val cid: String, val dataSource: DataSource, val mbp: MimeBodyPart) {
  def getCid: String = this.cid

  def getDataSource: DataSource = this.dataSource

  def getMbp: MimeBodyPart = this.mbp

  override def equals(obj: Any): Boolean = if (this eq obj.asInstanceOf[InlineImage]) true
  else if (!obj.isInstanceOf[InlineImage]) false
  else {
    val that = obj.asInstanceOf[InlineImage]
    this.cid == that.cid
  }

  override def hashCode: Int = this.cid.hashCode
}