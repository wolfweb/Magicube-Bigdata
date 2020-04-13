package com.magicube.eventflows.Job

abstract class JobService {
  def start: Unit

  def stop(): Unit = {
    _squartz.deleteAllJobs
    _squartz.shutdown
  }

  protected implicit val _squartz: Squartz = Squartz.build.start
}
