package com.magicube.eventflows.Job

import com.magicube.eventflows.Retry
import org.quartz.{Job, JobExecutionContext}
import org.slf4j.LoggerFactory

abstract class JobBase extends Job {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  def startJob(context: JobExecutionContext): Unit

  override def execute(context: JobExecutionContext): Unit = {
    try {
      Retry.retry(5, 2000)(startJob(context))
    } catch {
      case e: NullPointerException => logger.error("%s\n%s".format(e.getMessage, e.getStackTrace.mkString))
      case e: Exception => logger.error("%s\n%s".format(e.getMessage, e.getStackTrace.mkString))
    }
  }
}