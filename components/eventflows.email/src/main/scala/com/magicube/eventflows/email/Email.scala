package com.magicube.eventflows.email

import java.io.File
import java.util.List

case class EmailConf
(
  sender: String,
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

  object send {
    def a(mail: Mail) (implicit conf: EmailConf){
      val format =
        if (mail.attachment.isDefined) MultiPart
        else if (mail.richMessage.isDefined) Rich
        else Plain

      val commonsMail: EmailProvider = format match {
        case Plain => new SimpleEmail().setMsg(mail.message)
        case Rich => new HtmlEmail().setHtmlMsg(mail.richMessage.get).setTextMsg(mail.message)
        case MultiPart => {
          val attachment = EmailAttachment(
            path = mail.attachment.get.getAbsolutePath,
            disposition = EmailAttachment.ATTACHMENT,
            name = mail.attachment.get.getName
          )
          new MultiPartEmail().attach(attachment).setMsg(mail.message)
        }
      }

      mail.to foreach (commonsMail.addTo(_))
      mail.cc foreach (commonsMail.addCc(_))
      mail.bcc foreach (commonsMail.addBcc(_))

      commonsMail.setAuthentication(conf.senderMail, conf.senderPwd)
      commonsMail.setHostName(conf.senderServer)

      commonsMail
        .setFrom(mail.from._1, mail.from._2)
        .setSubject(mail.subject)
        .send
    }
  }

}
