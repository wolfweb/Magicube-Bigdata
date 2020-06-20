package com.magicube.eventflows.email

import java.io.{File, IOException}
import java.net.URL

import com.magicube.eventflows._
import javax.activation.{DataHandler, DataSource, FileDataSource, URLDataSource}
import javax.mail.BodyPart
import javax.mail.internet.{MimeBodyPart, MimeMultipart, MimePart}

class MultiPartEmail() extends EmailProvider {
  private var container: MimeMultipart = null
  private var primaryBodyPart: BodyPart = null
  private var subType: String = null
  private var initialized = false
  private var boolHasAttachments = false

  def setSubType(aSubType: String): Unit = {
    this.subType = aSubType
  }

  def getSubType: String = this.subType

  def addPart(partContent: String, partContentType: String): EmailProvider = {
    val bodyPart = this.createBodyPart
    try {
      bodyPart.setContent(partContent, partContentType)
      this.getContainer.addBodyPart(bodyPart)
      this
    } catch {
      case e => throw e
    }
  }

  def addPart(multipart: MimeMultipart): EmailProvider = {
    try {
      this.addPart(multipart, this.getContainer.getCount)
    } catch {
      case e => throw e
    }
  }

  def addPart(multipart: MimeMultipart, index: Int): EmailProvider = {
    val bodyPart = this.createBodyPart
    try {
      bodyPart.setContent(multipart)
      this.getContainer.addBodyPart(bodyPart, index)
      this
    } catch {
      case e => throw e
    }
  }

  protected def init(): Unit = {
    if (this.initialized) throw new IllegalStateException("Already initialized")

    this.container = this.createMimeMultipart
    super.setContent(this.container)
    this.initialized = true
  }

  override def setMsg(msg: String): EmailProvider = {
    if (msg.isNullOrEmpty) throw new EmailException("Invalid message supplied")
    try {
      val primary = this.getPrimaryBodyPart
      if (primary.isInstanceOf[MimePart] && !this.charset.isNullOrEmpty)
        primary.asInstanceOf[MimePart].setText(msg, this.charset)
      else
        primary.setText(msg)

      this
    } catch {
      case e => throw e
    }
  }

  override def buildMimeMessage(): Unit = {
    try {
      if (this.primaryBodyPart != null) {
        val body = this.getPrimaryBodyPart
        try body.getContent
        catch {
          case e: IOException => e.printStackTrace()
        }
      }
      if (this.subType != null)
        this.getContainer.setSubType(this.subType)
      super.buildMimeMessage()
    } catch {
      case e => throw e
    }
  }

  def attach(file: File): MultiPartEmail = {
    val fileName = file.getAbsolutePath
    try {
      if (!file.exists) throw new IOException("\"" + fileName + "\" does not exist")

      val fds = new FileDataSource(file)
      this.attach(fds, file.getName, null.asInstanceOf[String], "attachment")
    } catch {
      case e => throw e
    }
  }

  def attach(attachment: EmailAttachment): MultiPartEmail = {
    var result: MultiPartEmail = null
    if (attachment == null) throw new EmailException("Invalid attachment supplied")
    val url = attachment.url
    if (url == null) {
      var fileName: String = null
      try {
        fileName = attachment.path
        val file = new File(fileName)
        if (!file.exists) throw new IOException("\"" + fileName + "\" does not exist")
        result = this.attach(new FileDataSource(file), attachment.name, attachment.description, attachment.disposition)
      } catch {
        case e => throw e
      }
    }
    else result = this.attach(url, attachment.name, attachment.description, attachment.disposition)
    result
  }

  def attach(url: URL, name: String, description: String): MultiPartEmail = {
    this.attach(url, name, description, "attachment")
  }

  def attach(url: URL, name: String, description: String, disposition: String): MultiPartEmail = {
    try {
      val is = url.openStream
      is.close
    } catch {
      case e: IOException => throw new EmailException("Invalid URL set:" + url, e)
    }
    this.attach(new URLDataSource(url), name, description, disposition)
  }

  def attach(ds: DataSource, name: String, description: String): MultiPartEmail = {
    try {
      val is = if (ds != null) ds.getInputStream else null
      if (is != null) is.close
      if (is == null) throw new EmailException("Invalid Datasource")
    } catch {
      case var5: IOException => throw new EmailException("Invalid Datasource", var5)
    }
    this.attach(ds, name, description, "attachment")
  }

  def attach(ds: DataSource, name: String, description: String, disposition: String): MultiPartEmail = {
    var _name = name
    if (_name.isNullOrEmpty) _name = ds.getName
    val bodyPart = this.createBodyPart
    try {
      bodyPart.setDisposition(disposition)
      bodyPart.setFileName(_name.urlEncode)
      bodyPart.setDescription(description)
      bodyPart.setDataHandler(new DataHandler(ds))
      this.getContainer.addBodyPart(bodyPart)
    } catch {
      case e => throw e
    }
    this.setBoolHasAttachments(true)
    this
  }

  protected def getPrimaryBodyPart: BodyPart = {
    if (!this.initialized) this.init()
    if (this.primaryBodyPart == null) {
      this.primaryBodyPart = this.createBodyPart
      this.getContainer.addBodyPart(this.primaryBodyPart, 0)
    }
    this.primaryBodyPart
  }

  protected def getContainer: MimeMultipart = {
    if (!this.initialized) this.init()
    this.container
  }

  protected def createBodyPart = new MimeBodyPart()

  protected def createMimeMultipart = new MimeMultipart()

  def isBoolHasAttachments: Boolean = this.boolHasAttachments

  def setBoolHasAttachments(b: Boolean): Unit = {
    this.boolHasAttachments = b
  }

  protected def isInitialized: Boolean = this.initialized

  protected def setInitialized(b: Boolean): Unit = {
    this.initialized = b
  }
}
