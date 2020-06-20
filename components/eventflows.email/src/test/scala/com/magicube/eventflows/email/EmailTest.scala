package com.magicube.eventflows.email

import org.junit.Test
import com.magicube.eventflows.email.Email.{Mail, send}
import scala.collection.JavaConverters._

class EmailTest {
  implicit val conf: EmailConf = EmailConf (
    "changwei",
    "changwei@we7.cn",
    "ibelieveicanfly",
    "smtp.exmail.qq.com",
    List[String]("changwei@we7.cn").asJava
  )

  @Test
  def func_email_test(): Unit = {
    send a Mail(
      from = "changwei@we7.cn"->"changwei",
      to = List[String]("changwei@we7.cn"),
      subject = "测试",
      message = "Please find attach the latest strategy document.",
      richMessage = Some("内容测试 <blink>latest</blink> <strong>Strategy</strong>...")
    )
  }
}
