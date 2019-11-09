package com.magicube.eventflows.Repository.Slick.Conf

import slick.jdbc.JdbcProfile

case class Config
(
  name: String,
  conf: JdbcConfig,
  driver: JdbcProfile,
  rollbackTxError: Error,
  validationQuery: String,
  rowLockTimeoutError: Error
)
