package com.magicube.eventflows.Net

import java.io.File
import org.apache.commons.mail._

case class EmailConf
(
  sender: String,
  subject: String,
  senderMail: String,
  senderPwd: String,
  senderServer: String,
  receivers: List[String]
)

object Email {
  implicit def stringToSeq(single: String): Seq[String] = Seq(single)

  implicit def liftToOption[T](t: T): Option[T] = Some(t)

  sealed abstract class MailType

  case object Plain extends MailType

  case object Rich extends MailType

  case object MultiPart extends MailType

  case class Mail
  (
    from: (String, String),
    to: Seq[String],
    cc: Seq[String] = Seq.empty,
    bcc: Seq[String] = Seq.empty,
    subject: String,
    message: String = "统计消息",
    richMessage: Option[String] = None,
    attachment: Option[(File)] = None
  )

  case class send(conf:EmailConf) {
    def a(mail: Mail) {
      val format =
        if (mail.attachment.isDefined) MultiPart
        else if (mail.richMessage.isDefined) Rich
        else Plain

      val commonsMail: Email = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
        case MultiPart => {
          val attachment = new EmailAttachment()
          attachment.setPath(mail.attachment.get.getAbsolutePath)
          attachment.setDisposition(EmailAttachment.ATTACHMENT)
          attachment.setName(mail.attachment.get.getName)
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
        }
      }

      mail.to foreach (commonsMail.addTo(_))
      mail.cc foreach (commonsMail.addCc(_))
      mail.bcc foreach (commonsMail.addBcc(_))

      commonsMail.setAuthentication(conf.senderMail, conf.senderPwd)
      commonsMail.setHostName(conf.senderServer)

      commonsMail.setCharset("utf8")

      commonsMail
        .setFrom(mail.from._1, mail.from._2)
        .setSubject(mail.subject)
        .send()
    }
  }
}
