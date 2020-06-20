package com.magicube.eventflows.email

import javax.mail.{Authenticator, PasswordAuthentication}

class DefaultAuthenticator(val userName: String, val password: String) extends Authenticator {
  var authentication = new PasswordAuthentication(userName, password)
  override protected def getPasswordAuthentication: PasswordAuthentication = this.authentication
}
