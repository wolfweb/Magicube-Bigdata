package com.magicube.eventflows.Job

import java.time.Instant
import java.util.{Date, UUID}

import com.magicube.eventflows.Job.Squartz._
import org.junit.Test
import org.quartz.JobExecutionContext

class JobTaskTest extends JobService{
  @Test
  def func_task_func() = {
    val foo = FooEntity(UUID.randomUUID().toString)

    val start = Date.from(Instant.now())
    schedSimpleOnce[FooJob](start, jobDataMapOpt = Some(Map[String, Any]("entity" -> foo)))

    Thread.sleep(500)
    assert(foo.name == "wolfweb")

    stop()
  }

  override def start: Unit = {

  }
}

case class FooEntity(var name: String)

case class FooJob() extends JobBase {
  override def startJob(context: JobExecutionContext): Unit = {
    val entity = context.getMergedJobDataMap.get("entity").asInstanceOf[FooEntity]
    entity.name = "wolfweb"
  }
}