package com.magicube.eventflows.email

import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util
import java.util.{Date, Properties}

import com.magicube.eventflows._
import javax.mail.Message.RecipientType
import javax.mail.internet.{InternetAddress, MimeMessage, MimeMultipart, MimeUtility}
import javax.mail.{Address, Authenticator, MessagingException, Session, Transport}
import javax.naming.{Context, InitialContext}

import scala.collection.JavaConverters._

abstract case class EmailProvider
(
  var debug: Boolean = false,
  var subject: String = null,
  var charset: String = Charset.forName("utf8").name()
) {
  protected var message: MimeMessage = null
  protected var fromAddress: InternetAddress = null
  protected var emailBody: MimeMultipart = null
  protected var content: Any = null
  protected var contentType: String = null
  protected var sentDate: Date = null
  protected var authenticator: Authenticator = null
  protected var hostName: String = null
  protected var smtpPort = "25"
  protected var sslSmtpPort = "465"
  protected var toList = new util.ArrayList[InternetAddress]()
  protected var ccList = new util.ArrayList[InternetAddress]()
  protected var bccList = new util.ArrayList[InternetAddress]()
  protected var replyList = new util.ArrayList[InternetAddress]()
  protected var headers = new util.HashMap[String, String]
  protected var bounceAddress: String = null
  protected var popBeforeSmtp = false
  protected var popHost: String = null
  protected var popUsername: String = null
  protected var popPassword: String = null
  protected var socketTimeout = 60000
  protected var socketConnectionTimeout = 60000
  private var startTlsEnabled = false
  private var startTlsRequired = false
  private var sslOnConnect = false
  private var sslCheckServerIdentity = false
  private var sendPartial = false
  private var session: Session = null

  def setAuthentication(userName: String, password: String): Unit = {
    this.setAuthenticator(new DefaultAuthenticator(userName, password))
  }

  def setAuthenticator(newAuthenticator: Authenticator): Unit = {
    this.authenticator = newAuthenticator
  }

  def setContent(aMimeMultipart: MimeMultipart): Unit = {
    this.emailBody = aMimeMultipart
  }

  def setContent(aObject: Any, aContentType: String): Unit = {
    this.content = aObject
    this.updateContentType(aContentType)
  }

  def updateContentType(aContentType: String): Unit = {
    if (aContentType.isNullOrEmpty)
      this.contentType = null
    else {
      this.contentType = aContentType
      val strMarker = "; charset="
      var charsetPos = aContentType.toLowerCase.indexOf(strMarker)
      if (charsetPos != -1) {
        charsetPos += strMarker.length
        val intCharsetEnd = aContentType.toLowerCase.indexOf(" ", charsetPos)
        if (intCharsetEnd != -1) this.charset = aContentType.substring(charsetPos, intCharsetEnd)
        else this.charset = aContentType.substring(charsetPos)
      }
      else if (this.contentType.startsWith("text/") && this.charset.isNullOrEmpty) {
        val contentTypeBuf = new StringBuffer(this.contentType)
        contentTypeBuf.append(strMarker)
        contentTypeBuf.append(this.charset)
        this.contentType = contentTypeBuf.toString
      }
    }
  }

  def setHostName(aHostName: String): Unit = {
    this.checkSessionAlreadyInitialized()
    this.hostName = aHostName
  }

  def setStartTLSEnabled(startTlsEnabled: Boolean): EmailProvider = {
    this.checkSessionAlreadyInitialized()
    this.startTlsEnabled = startTlsEnabled
    this
  }

  def setStartTLSRequired(startTlsRequired: Boolean): EmailProvider = {
    this.checkSessionAlreadyInitialized()
    this.startTlsRequired = startTlsRequired
    this
  }

  def setSmtpPort(aPortNumber: Int): Unit = {
    this.checkSessionAlreadyInitialized()
    if (aPortNumber < 1) throw new IllegalArgumentException("Cannot connect to a port number that is less than 1 ( " + aPortNumber + " )")
    else this.smtpPort = Integer.toString(aPortNumber)
  }

  def setMailSession(aSession: Session): Unit = {
    if (aSession == null) throw new IllegalArgumentException("no mail session supplied")
    val sessionProperties = aSession.getProperties
    val auth = sessionProperties.getProperty("mail.smtp.auth")
    if ("true".equalsIgnoreCase(auth)) {
      val userName = sessionProperties.getProperty("mail.smtp.user")
      val password = sessionProperties.getProperty("mail.smtp.password")
      if (!userName.isNullOrEmpty && !password.isNullOrEmpty) {
        this.authenticator = new DefaultAuthenticator(userName, password)
        this.session = Session.getInstance(sessionProperties, this.authenticator)
      }
      else
        this.session = aSession
    }
    else
      this.session = aSession
  }

  def setMailSessionFromJNDI(jndiName: String): Unit = {
    if (jndiName.isNullOrEmpty) throw new IllegalArgumentException("JNDI name missing")
    else {
      var ctx: Context = null
      if (jndiName.startsWith("java:"))
        ctx = new InitialContext()
      else ctx = (new InitialContext).lookup("java:comp/env").asInstanceOf[Context]
      this.setMailSession(ctx.lookup(jndiName).asInstanceOf[Session])
    }
  }

  def getMailSession: Session = {
    if (this.session == null) {
      val properties = new Properties(System.getProperties)
      properties.setProperty("mail.transport.protocol", "smtp")

      if (this.hostName.isNullOrEmpty) this.hostName = properties.getProperty("mail.smtp.host")
      if (this.hostName.isNullOrEmpty) throw new EmailException("Cannot find valid hostname for mail session")

      properties.setProperty("mail.smtp.port", this.smtpPort)
      properties.setProperty("mail.smtp.host", this.hostName)
      properties.setProperty("mail.debug", String.valueOf(this.debug))
      properties.setProperty("mail.smtp.starttls.enable", if (this.isStartTLSEnabled) "true" else "false")
      properties.setProperty("mail.smtp.starttls.required", if (this.isStartTLSRequired) "true" else "false")
      properties.setProperty("mail.smtp.sendpartial", if (this.isSendPartial) "true" else "false")
      properties.setProperty("mail.smtps.sendpartial", if (this.isSendPartial) "true" else "false")
      if (this.authenticator != null) properties.setProperty("mail.smtp.auth", "true")
      if (this.isSSLOnConnect) {
        properties.setProperty("mail.smtp.port", this.sslSmtpPort)
        properties.setProperty("mail.smtp.socketFactory.port", this.sslSmtpPort)
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        properties.setProperty("mail.smtp.socketFactory.fallback", "false")
      }
      if ((this.isSSLOnConnect || this.isStartTLSEnabled) && this.isSSLCheckServerIdentity)
        properties.setProperty("mail.smtp.ssl.checkserveridentity", "true")

      if (this.bounceAddress != null)
        properties.setProperty("mail.smtp.from", this.bounceAddress)

      if (this.socketTimeout > 0)
        properties.setProperty("mail.smtp.timeout", Integer.toString(this.socketTimeout))

      if (this.socketConnectionTimeout > 0)
        properties.setProperty("mail.smtp.connectiontimeout", Integer.toString(this.socketConnectionTimeout))

      this.session = Session.getInstance(properties, this.authenticator)
    }
    this.session
  }

  def setFrom(email: String): EmailProvider = this.setFrom(email, null.asInstanceOf[String])

  def setFrom(email: String, name: String): EmailProvider = this.setFrom(email, name, this.charset)

  def setFrom(email: String, name: String, charset: String): EmailProvider = {
    this.fromAddress = this.createInternetAddress(email, name, charset)
    this
  }

  def addTo(email: String): EmailProvider = this.addTo(email, null.asInstanceOf[String])

  def addTo(emails: String*): EmailProvider = {
    if (emails != null && emails.length != 0) {
      val var2 = emails
      val var3 = emails.length
      for (var4 <- 0 until var3) {
        val email = var2(var4)
        this.addTo(email, null.asInstanceOf[String])
      }
      this
    }
    else throw new EmailException("Address List provided was invalid")
  }

  def addTo(email: String, name: String): EmailProvider = this.addTo(email, name, this.charset)

  def addTo(email: String, name: String, charset: String): EmailProvider = {
    this.toList.add(this.createInternetAddress(email, name, charset))
    this
  }

  def setTo(aCollection: util.Collection[InternetAddress]): EmailProvider = {
    if (aCollection != null && !aCollection.isEmpty) {
      this.toList = new util.ArrayList[InternetAddress](aCollection)
      this
    }
    else throw new EmailException("Address List provided was invalid")
  }

  def addCc(email: String): EmailProvider = this.addCc(email, null.asInstanceOf[String])

  def addCc(emails: String*): EmailProvider = {
    if (emails != null && emails.length != 0) {
      val var2 = emails
      val var3 = emails.length
      for (var4 <- 0 until var3) {
        val email = var2(var4)
        this.addCc(email, null.asInstanceOf[String])
      }
      this
    }
    else throw new EmailException("Address List provided was invalid")
  }

  def addCc(email: String, name: String): EmailProvider = this.addCc(email, name, this.charset)

  def addCc(email: String, name: String, charset: String): EmailProvider = {
    this.ccList.add(this.createInternetAddress(email, name, charset))
    this
  }

  def setCc(aCollection: util.Collection[InternetAddress]): EmailProvider = {
    if (aCollection != null && !aCollection.isEmpty) {
      this.ccList = new util.ArrayList[InternetAddress](aCollection)
      this
    }
    else throw new EmailException("Address List provided was invalid")
  }

  def addBcc(email: String): EmailProvider = this.addBcc(email, null.asInstanceOf[String])

  def addBcc(emails: String*): EmailProvider = {
    if (emails != null && emails.length != 0) {
      val var2 = emails
      val var3 = emails.length
      for (var4 <- 0 until var3) {
        val email = var2(var4)
        this.addBcc(email, null.asInstanceOf[String])
      }
      this
    }
    else throw new EmailException("Address List provided was invalid")
  }

  def addBcc(email: String, name: String): EmailProvider = this.addBcc(email, name, this.charset)

  def addBcc(email: String, name: String, charset: String): EmailProvider = {
    this.bccList.add(this.createInternetAddress(email, name, charset))
    this
  }

  def setBcc(aCollection: util.Collection[InternetAddress]): EmailProvider = {
    if (aCollection != null && !aCollection.isEmpty) {
      this.bccList = new util.ArrayList[InternetAddress](aCollection)
      this
    }
    else throw new EmailException("Address List provided was invalid")
  }

  def addReplyTo(email: String): EmailProvider = this.addReplyTo(email, null.asInstanceOf[String])

  def addReplyTo(email: String, name: String): EmailProvider = this.addReplyTo(email, name, this.charset)

  def addReplyTo(email: String, name: String, charset: String): EmailProvider = {
    this.replyList.add(this.createInternetAddress(email, name, charset))
    this
  }

  def setReplyTo(aCollection: util.Collection[InternetAddress]): EmailProvider = {
    if (aCollection != null && !aCollection.isEmpty) {
      this.replyList = new util.ArrayList[InternetAddress](aCollection)
      this
    }
    else
      throw new EmailException("Address List provided was invalid")
  }

  def setHeaders(map: util.Map[String, String]): Unit = {
    this.headers.clear()
    for ((k, v) <- map.asScala) {
      this.addHeader(k, v)
    }
  }

  def addHeader(name: String, value: String): Unit = {
    if (name.isNullOrEmpty) throw new IllegalArgumentException("name can not be null or empty")
    else if (value.isNullOrEmpty) throw new IllegalArgumentException("value can not be null or empty")
    else this.headers.put(name, value)
  }

  def getHeader(header: String): String = this.headers.get(header)

  def getHeaders: util.Map[String, String] = this.headers

  def setSubject(aSubject: String): EmailProvider = {
    this.subject = aSubject
    this
  }

  def getBounceAddress: String = this.bounceAddress

  def setBounceAddress(email: String): EmailProvider = {
    this.checkSessionAlreadyInitialized()
    if (email != null && !email.isEmpty)
      try this.bounceAddress = this.createInternetAddress(email, null, this.charset).getAddress
      catch {
        case e: EmailException => throw new IllegalArgumentException("Failed to set the bounce address : " + email, e)
      }
    else this.bounceAddress = email
    this
  }

  def setMsg(var1: String): EmailProvider

  def buildMimeMessage(): Unit = {
    if (this.message != null) throw new IllegalStateException("The MimeMessage is already built.")
    try {
      this.message = this.createMimeMessage(this.getMailSession)

      if (!this.subject.isNullOrEmpty)
        if (!this.charset.isNullOrEmpty)
          this.message.setSubject(this.subject, this.charset)
        else
          this.message.setSubject(this.subject)

      this.updateContentType(this.contentType)

      if (this.content != null)
        if ("text/plain".equalsIgnoreCase(this.contentType) && this.content.isInstanceOf[String])
          this.message.setText(this.content.toString, this.charset)
        else
          this.message.setContent(this.content, this.contentType)
      else if (this.emailBody != null)
        if (this.contentType == null)
          this.message.setContent(this.emailBody)
        else
          this.message.setContent(this.emailBody, this.contentType)
      else
        this.message.setText("")

      if (this.fromAddress != null)
        this.message.setFrom(this.fromAddress)
      else if (this.session.getProperty("mail.smtp.from") == null && this.session.getProperty("mail.from") == null)
        throw new EmailException("From address required")

      if (this.toList.size + this.ccList.size + this.bccList.size == 0)
        throw new EmailException("At least one receiver address required")
      else {
        if (this.toList.size > 0) this.message.setRecipients(RecipientType.TO, toInternetAddressArray(this.toList))
        if (this.ccList.size > 0) this.message.setRecipients(RecipientType.CC, toInternetAddressArray(this.ccList))
        if (this.bccList.size > 0) this.message.setRecipients(RecipientType.BCC, toInternetAddressArray(this.bccList))
        if (this.replyList.size > 0) this.message.setReplyTo(toInternetAddressArray(replyList))
        if (this.headers.size > 0) {
          for ((k, v) <- this.headers.asScala) {
            val foldedValue = this.createFoldedHeaderValue(k, v)
            this.message.addHeader(k, foldedValue)
          }
        }
        if (this.message.getSentDate == null) this.message.setSentDate(this.getSentDate)
        if (this.popBeforeSmtp) {
          val store = this.session.getStore("pop3")
          store.connect(this.popHost, this.popUsername, this.popPassword)
        }
      }
    } catch {
      case e: MessagingException => throw e
    }
  }

  def sendMimeMessage: String = {
    if (this.message == null) throw new IllegalArgumentException("MimeMessage has not been created yet")
    try {
      Transport.send(this.message)
      this.message.getMessageID
    } catch {
      case e: Throwable => {
        val msg = "Sending the email to the following server failed : " + this.getHostName + ":" + this.getSmtpPort
        throw new EmailException(msg, e)
      }
    }
  }

  def getMimeMessage: MimeMessage = this.message

  def send: String = {
    this.buildMimeMessage()
    this.sendMimeMessage
  }

  def setSentDate(date: Date): Unit = {
    if (date != null) this.sentDate = new Date(date.getTime)
  }

  def getSentDate: Date = {
    if (this.sentDate == null) new Date
    else new Date(this.sentDate.getTime)
  }

  def getFromAddress: InternetAddress = this.fromAddress

  def getHostName: String = {
    if (this.session != null) this.session.getProperty("mail.smtp.host")
    else if (!this.hostName.isNullOrEmpty) this.hostName
    else null
  }

  def getSmtpPort: String = {
    if (this.session != null) this.session.getProperty("mail.smtp.port")
    else if (!this.smtpPort.isNullOrEmpty) this.smtpPort
    else null
  }

  def isStartTLSRequired: Boolean = this.startTlsRequired

  def isStartTLSEnabled: Boolean = this.startTlsEnabled

  protected def toInternetAddressArray(list: util.List[InternetAddress]): Array[Address] = {
    list.asScala.toArray[Address]
  }

  def setPopBeforeSmtp(newPopBeforeSmtp: Boolean, newPopHost: String, newPopUsername: String, newPopPassword: String): Unit = {
    this.popBeforeSmtp = newPopBeforeSmtp
    this.popHost = newPopHost
    this.popUsername = newPopUsername
    this.popPassword = newPopPassword
  }

  def isSSLOnConnect: Boolean = this.sslOnConnect

  def setSSLOnConnect(ssl: Boolean): EmailProvider = {
    this.checkSessionAlreadyInitialized()
    this.sslOnConnect = ssl
    this
  }

  def isSSLCheckServerIdentity: Boolean = this.sslCheckServerIdentity

  def setSSLCheckServerIdentity(sslCheckServerIdentity: Boolean): EmailProvider = {
    this.checkSessionAlreadyInitialized()
    this.sslCheckServerIdentity = sslCheckServerIdentity
    this
  }

  def getSslSmtpPort: String = {
    if (this.session != null) this.session.getProperty("mail.smtp.socketFactory.port")
    else if (!this.sslSmtpPort.isNullOrEmpty) this.sslSmtpPort
    else null
  }

  def setSslSmtpPort(sslSmtpPort: String): Unit = {
    this.checkSessionAlreadyInitialized()
    this.sslSmtpPort = sslSmtpPort
  }

  def isSendPartial: Boolean = this.sendPartial

  def setSendPartial(sendPartial: Boolean): EmailProvider = {
    this.checkSessionAlreadyInitialized()
    this.sendPartial = sendPartial
    this
  }

  def getToAddresses: util.List[InternetAddress] = this.toList

  def getCcAddresses: util.List[InternetAddress] = this.ccList

  def getBccAddresses: util.List[InternetAddress] = this.bccList

  def getReplyToAddresses: util.List[InternetAddress] = this.replyList

  def getSocketConnectionTimeout: Int = this.socketConnectionTimeout

  def setSocketConnectionTimeout(socketConnectionTimeout: Int): Unit = {
    this.checkSessionAlreadyInitialized()
    this.socketConnectionTimeout = socketConnectionTimeout
  }

  def getSocketTimeout: Int = this.socketTimeout

  def setSocketTimeout(socketTimeout: Int): Unit = {
    this.checkSessionAlreadyInitialized()
    this.socketTimeout = socketTimeout
  }

  protected def createMimeMessage(aSession: Session) = new MimeMessage(aSession)

  private def createFoldedHeaderValue(name: String, value: String) = {
    if (name.isNullOrEmpty) throw new IllegalArgumentException("name can not be null or empty")

    if (!value.isNullOrEmpty)
      try
        MimeUtility.fold(name.length + 2, MimeUtility.encodeText(value, this.charset, null))
      catch {
        case _: UnsupportedEncodingException => value
      }
    else throw new IllegalArgumentException("value can not be null or empty")
  }

  private def createInternetAddress(email: String, name: String, charsetName: String) = {
    try {
      val address = new InternetAddress(email)
      if (!name.isNullOrEmpty)
        if (charsetName.isNullOrEmpty)
          address.setPersonal(name)
        else {
          val set = Charset.forName(charsetName)
          address.setPersonal(name, set.name)
        }
      address.validate()
      address
    } catch {
      case e: Throwable => throw e
    }
  }

  private def checkSessionAlreadyInitialized(): Unit = {
    if (this.session != null) throw new IllegalStateException("The mail session is already initialized")
  }
}
