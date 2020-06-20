package com.magicube.eventflows.Job

import java.util.Date

import com.magicube.eventflows.Date.DateFactory._
import org.joda.time.DateTime
import org.quartz.JobBuilder._
import org.quartz.TriggerBuilder._
import org.quartz._
import org.quartz.impl._
import org.quartz.impl.matchers.GroupMatcher

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

object Squartz {

  case class JdbcConfig
  (
    val name: String,
    val driver: String,
    val url: String,
    val user: String,
    val password: String
  )

  implicit def mapToJProps(map: Map[String, String]): java.util.Properties = {
    val javaProperties = new java.util.Properties
    for ((key, value) <- map) {
      javaProperties.setProperty(key, value)
    }
    javaProperties
  }

  def build: Squartz = {
    val scheduler = StdSchedulerFactory.getDefaultScheduler
    new Squartz(scheduler)
  }

  def build(props: java.util.Properties): Squartz = {
    val schedFact = new StdSchedulerFactory(props)
    val scheduler = schedFact.getScheduler
    new Squartz(scheduler)
  }

  def build
  (
    name: String,
    threadCount: Int = 10,
    jobStore: String = "org.quartz.simpl.RAMJobStore",
    jdbcConfigOpt: Option[JdbcConfig] = None
  ): Squartz = {
    val props = Map[String, String](
      "org.quartz.scheduler.instanceName" -> name,
      "org.quartz.threadPool.threadCount" -> threadCount.toString,
      "org.quartz.jobStore.class" -> jobStore
    ) ++ (if (jobStore == "org.quartz.impl.jdbcjobstore.JobStoreTX") {
      jdbcConfigOpt match {
        case Some(jdbcConfig) =>
          val jdbcConfigNamespace = "org.quartz.dataSource." + jdbcConfig.name
          Map[String, String](
            "org.quartz.jobStore.dataSource" -> jdbcConfig.name,
            jdbcConfigNamespace + ".driver" -> jdbcConfig.driver,
            jdbcConfigNamespace + ".URL" -> jdbcConfig.url,
            jdbcConfigNamespace + ".user" -> jdbcConfig.user,
            jdbcConfigNamespace + ".password" -> jdbcConfig.password
          ) ++ (if (jdbcConfig.driver == "org.postgresql.Driver") {
            Map[String, String]("org.quartz.jobStore.driverDelegateClass" ->
              "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate")
          } else {
            Map[String, String]()
          })
        case None =>
          throw new Exception("Need to specify jdbcConfig for " + jobStore)
      }
    } else {
      Map[String, String]()
    })
    build(props)
  }

  def simpleBuilder[A <: Job](implicit squartz: Squartz, mA: ClassTag[A]) = SquartzSimpleBuilder.build[A]

  def cronBuilder[A <: Job]
  (
    cronStr: String
  )(implicit squartz: Squartz, mA: ClassTag[A]) = SquartzCronBuilder.build[A](cronStr)

  def schedCron[A <: Job]
  (
    cronStr: String,
    startDateOpt: Option[Date] = None,
    endDateOpt: Option[Date] = None,
    jobIdentOpt: Option[(String, Option[String])] = None,
    triggerIdentOpt: Option[(String, Option[String])] = None,
    jobDataMapOpt: Option[Map[String, Any]] = None,
    triggerDataMapOpt: Option[Map[String, Any]] = None
  )(implicit squartz: Squartz, mA: ClassTag[A]): (Date, (String, String), (String, String)) = {

    val builder = cronBuilder[A](cronStr)

    configureBuilder(
      builder,
      startDateOpt, endDateOpt,
      jobIdentOpt, triggerIdentOpt,
      jobDataMapOpt, triggerDataMapOpt
    )
    runBuilder(builder)
  }

  def schedSimpleForeverExclusive[A <: Job]
  (
    repeatInterval: Int,
    repeatUnit: Time,
    startDateOpt: Option[Date] = None,
    endDateOpt: Option[Date] = None,
    jobIdentOpt: Option[(String, Option[String])] = None,
    triggerIdentOpt: Option[(String, Option[String])] = None,
    jobDataMapOpt: Option[Map[String, Any]] = None,
    triggerDataMapOpt: Option[Map[String, Any]] = None
  )(implicit squartz: Squartz, mA: ClassTag[A]): (Date, (String, String), (String, String)) = {
    schedSimple[A](repeatInterval, repeatUnit, -1,
      startDateOpt, endDateOpt, jobIdentOpt, triggerIdentOpt,
      jobDataMapOpt, triggerDataMapOpt
    )
  }

  def schedSimpleForever[A <: Job]
  (
    repeatInterval: Int,
    repeatUnit: Time,
    startDateOpt: Option[Date] = None,
    endDateOpt: Option[Date] = None,
    jobIdentOpt: Option[(String, Option[String])] = None,
    triggerIdentOpt: Option[(String, Option[String])] = None,
    jobDataMapOpt: Option[Map[String, Any]] = None,
    triggerDataMapOpt: Option[Map[String, Any]] = None
  )(implicit squartz: Squartz, mA: ClassTag[A]): (Date, (String, String), (String, String)) = {
    schedSimple(repeatInterval, repeatUnit, -1,
      startDateOpt, endDateOpt, jobIdentOpt, triggerIdentOpt,
      jobDataMapOpt, triggerDataMapOpt
    )
  }

  def schedSimpleOnce[A <: Job]
  (
    date: Date,
    jobIdentOpt: Option[(String, Option[String])] = None,
    triggerIdentOpt: Option[(String, Option[String])] = None,
    jobDataMapOpt: Option[Map[String, Any]] = None,
    triggerDataMapOpt: Option[Map[String, Any]] = None
  )(implicit squartz: Squartz, mA: ClassTag[A]) = {
    schedSimple[A](
      0,
      SECONDS,
      1,
      Some(date),
      None,
      jobIdentOpt,
      triggerIdentOpt,
      jobDataMapOpt,
      triggerDataMapOpt
    )
  }

  def schedSimple[A <: Job]
  (
    repeatInterval: Int,
    repeatUnit: Time,
    totalCount: Int,
    startDateOpt: Option[Date] = None,
    endDateOpt: Option[Date] = None,
    jobIdentOpt: Option[(String, Option[String])] = None,
    triggerIdentOpt: Option[(String, Option[String])] = None,
    jobDataMapOpt: Option[Map[String, Any]] = None,
    triggerDataMapOpt: Option[Map[String, Any]] = None
  )(implicit squartz: Squartz, mA: ClassTag[A]): (Date, (String, String), (String, String)) = {
    val builder = repeatUnit match {
      case SECONDS =>
        if (totalCount >= 0) {
          SquartzSimpleBuilder.repeatSecondlyForTotalCount[A](totalCount, repeatInterval)
        } else {
          SquartzSimpleBuilder.repeatSecondlyForever[A](repeatInterval)
        }
      case MINUTES =>
        if (totalCount >= 0) {
          SquartzSimpleBuilder.repeatMinutelyForTotalCount[A](totalCount, repeatInterval)
        } else {
          SquartzSimpleBuilder.repeatMinutelyForever[A](repeatInterval)
        }
      case HOURS =>
        if (totalCount >= 0) {
          SquartzSimpleBuilder.repeatHourlyForTotalCount[A](totalCount, repeatInterval)
        } else {
          SquartzSimpleBuilder.repeatHourlyForever[A](repeatInterval)
        }
    }

    configureBuilder(
      builder,
      startDateOpt, endDateOpt,
      jobIdentOpt, triggerIdentOpt,
      jobDataMapOpt, triggerDataMapOpt
    )
    runBuilder(builder)
  }

  private def runBuilder(builder: SquartzBuilder[_, _]): (Date, (String, String), (String, String)) = {
    val (schedDate, trigger, jobDetailOpt) = builder.sched
    val triggerKey = trigger.getKey
    val jobKey = trigger.getJobKey

    (schedDate, (triggerKey.getName, triggerKey.getGroup), (jobKey.getName, jobKey.getGroup))
  }

  private def configureBuilder
  (
    builder: SquartzBuilder[_, _],
    startDateOpt: Option[Date] = None,
    endDateOpt: Option[Date] = None,
    jobIdentOpt: Option[(String, Option[String])] = None,
    triggerIdentOpt: Option[(String, Option[String])] = None,
    jobDataMapOpt: Option[Map[String, Any]] = None,
    triggerDataMapOpt: Option[Map[String, Any]] = None
  ) {

    jobIdentOpt.foreach(jobIdent => {
      val (name, groupOpt) = jobIdent
      groupOpt match {
        case Some(group) =>
          builder.jobWithIdentity(name, group)
        case None =>
          builder.jobWithIdentity(name)
      }
    })

    triggerIdentOpt.foreach(triggerIdent => {
      val (name, groupOpt) = triggerIdent
      groupOpt match {
        case Some(group) =>
          builder.triggerWithIdentity(name, group)
        case None =>
          builder.triggerWithIdentity(name)
      }
    })

    startDateOpt.foreach(startDate => builder.triggerStartAt(startDate))
    endDateOpt.foreach(endDate => builder.triggerEndAt(endDate))

    jobDataMapOpt.foreach(dataMap => builder.jobUsingJobData(new JobDataMap(dataMap)))
    triggerDataMapOpt.foreach(dataMap => builder.triggerUsingJobData(new JobDataMap(dataMap)))
  }
}

class Squartz
(
  scheduler: Scheduler
) {

  def start = {
    scheduler.start;
    this;
  }

  def sched
  (
    trigger: Trigger
  ): Date = {
    scheduler.scheduleJob(trigger)
  }

  def sched
  (
    jobDetail: JobDetail,
    trigger: Trigger
  ): Date = {
    scheduler.scheduleJob(jobDetail, trigger)
  }

  def resumeAllJobs(): Squartz = {
    val jobs = getAllJobs
    for (job <- jobs) {
      val triggers = getTriggersForJob(job.getKey)
      for (trigger <- triggers) {
        val triggerTime = new DateTime(trigger.getFinalFireTime.getTime)
        if (subSeconds(triggerTime) > 0) {
          scheduler.resumeJob(trigger.getJobKey)
          scheduler.resumeTrigger(trigger.getKey)
        } else {
          deleteJob(job.getKey.getName)
        }
      }
    }
    this
  }

  def getJobDetail(jobName: String, jobGroup: String): Option[JobDetail] = {
    val jobDetail = scheduler.getJobDetail(new JobKey(jobName, jobGroup))
    if (jobDetail != null) {
      Some(jobDetail)
    } else {
      None
    }
  }

  def getTriggersForJob(jobName: String, jobGroup: String): Seq[Trigger] = getTriggersForJob(new JobKey(jobName, jobGroup))

  def getTriggersForJob(jobKey: JobKey): Seq[Trigger] = scheduler.getTriggersOfJob(jobKey)

  def getJobGroupNames: Seq[String] = scheduler.getJobGroupNames

  def getJobKeysForGroup(groupName: String): Seq[JobKey] = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).toArray(Array[JobKey]())

  def getAllJobKeys: Seq[JobKey] = getJobGroupNames.map(getJobKeysForGroup(_)).flatten

  def getJobsForGroup(groupName: String): Seq[JobDetail] = getJobKeysForGroup(groupName).map(getJob(_))

  def getAllJobs: Seq[JobDetail] = getAllJobKeys.map(getJob(_))

  def getJob(jobKey: JobKey): JobDetail = scheduler.getJobDetail(jobKey)

  def getJob(jobName: String, jobGroup: String = "DEFAULT"): JobDetail = scheduler.getJobDetail(new JobKey(jobName, jobGroup))

  def deleteAllJobs: Boolean = scheduler.deleteJobs(getAllJobKeys)

  def deleteJobsForGroup(jobGroup: String): Boolean = scheduler.deleteJobs(getJobKeysForGroup(jobGroup))

  def deleteJob(jobName: String, jobGroup: String = "DEFAULT"): Boolean = scheduler.deleteJob(new JobKey(jobName, jobGroup))

  def checkJobExists(jobName: String, jobGroup: String = "DEFAULT"): Boolean = scheduler.checkExists(new JobKey(jobName, jobGroup))

  def getName = scheduler.getSchedulerName

  def shutdown: Squartz = {
    scheduler.shutdown
    this
  }
}

abstract class Time

case object SECONDS extends Time

case object MINUTES extends Time

case object HOURS extends Time

class NoJob extends Job {
  override def execute(context: JobExecutionContext) {}
}

abstract class SquartzBuilder[T, U <: Job]
(
  private val squartz: Squartz
)(implicit mT: ClassTag[T], mU: ClassTag[U]) {

  val jobBuilderOpt: Option[JobBuilder] = {
    mU.runtimeClass.toString match {
      case "squartz.NoJob" =>
        None
      case jobClassName =>
        Some(newJob(mU.runtimeClass.asInstanceOf[Class[U]]))
    }
  }

  val triggerBuilder = newTrigger

  def jobRequestRecovery: T = {
    jobBuilderOpt.foreach(_.requestRecovery)
    this.asInstanceOf[T]
  }

  def jobRequestRecovery(jobShouldRecover: Boolean): T = {
    jobBuilderOpt.foreach(_.requestRecovery(jobShouldRecover))
    this.asInstanceOf[T]
  }

  def jobStoreDurably: T = {
    jobBuilderOpt.foreach(_.storeDurably)
    this.asInstanceOf[T]
  }

  def jobStoreDurably(jobDurability: Boolean): T = {
    jobBuilderOpt.foreach(_.storeDurably(jobDurability))
    this.asInstanceOf[T]
  }

  def jobUsingJobData(newJobDataMap: JobDataMap): T = {
    jobBuilderOpt.foreach(_.usingJobData(newJobDataMap))
    this.asInstanceOf[T]
  }

  def jobUsingJobData(dataKey: String, value: Double): T = {
    jobBuilderOpt.foreach(_.usingJobData(dataKey, value))
    this.asInstanceOf[T]
  }

  def jobUsingJobData(dataKey: String, value: Float): T = {
    jobBuilderOpt.foreach(_.usingJobData(dataKey, new java.lang.Float(value)))
    this.asInstanceOf[T]
  }

  def jobUsingJobData(dataKey: String, value: Int): T = {
    jobBuilderOpt.foreach(_.usingJobData(dataKey, new java.lang.Integer(value)))
    this.asInstanceOf[T]
  }

  def jobUsingJobData(dataKey: String, value: Long): T = {
    jobBuilderOpt.foreach(_.usingJobData(dataKey, new java.lang.Long(value)))
    this.asInstanceOf[T]
  }

  def jobUsingJobData(dataKey: String, value: String): T = {
    jobBuilderOpt.foreach(_.usingJobData(dataKey, value))
    this.asInstanceOf[T]
  }

  def jobWithDescription(jobDescription: String): T = {
    jobBuilderOpt.foreach(_.withDescription(jobDescription))
    this.asInstanceOf[T]
  }

  def jobWithIdentity(jobKey: JobKey): T = {
    jobBuilderOpt.foreach(_.withIdentity(jobKey))
    this.asInstanceOf[T]
  }

  def jobWithIdentity(name: String): T = {
    jobBuilderOpt.foreach(_.withIdentity(name))
    this.asInstanceOf[T]
  }

  def jobWithIdentity(name: String, group: String): T = {
    jobBuilderOpt.foreach(_.withIdentity(name, group))
    this.asInstanceOf[T]
  }

  def triggerEndAt(triggerEndTime: Date): T = {
    triggerBuilder.endAt(triggerEndTime)
    this.asInstanceOf[T]
  }

  def triggerForJob(jobDetail: JobDetail): T = {
    triggerBuilder.forJob(jobDetail)
    this.asInstanceOf[T]
  }

  def triggerForJob(keyOfJobToFire: JobKey): T = {
    triggerBuilder.forJob(keyOfJobToFire)
    this.asInstanceOf[T]
  }

  def triggerForJob(jobName: String): T = {
    triggerBuilder.forJob(jobName)
    this.asInstanceOf[T]
  }

  def triggerForJob(jobName: String, jobGroup: String): T = {
    triggerBuilder.forJob(jobName, jobGroup)
    this.asInstanceOf[T]
  }

  def triggerModifiedByCalendar(calName: String): T = {
    triggerBuilder.modifiedByCalendar(calName)
    this.asInstanceOf[T]
  }

  def triggerStartAt(triggerStartTime: Date): T = {
    triggerBuilder.startAt(triggerStartTime)
    this.asInstanceOf[T]
  }

  def triggerStartNow: T = {
    triggerBuilder.startNow
    this.asInstanceOf[T]
  }

  def triggerUsingJobData(newJobDataMap: JobDataMap): T = {
    triggerBuilder.usingJobData(newJobDataMap)
    this.asInstanceOf[T]
  }

  def triggerUsingJobData(dataKey: String, value: Double): T = {
    triggerBuilder.usingJobData(dataKey, value)
    this.asInstanceOf[T]
  }

  def triggerUsingJobData(dataKey: String, value: Float): T = {
    triggerBuilder.usingJobData(dataKey, new java.lang.Float(value))
    this.asInstanceOf[T]
  }

  def triggerUsingJobData(dataKey: String, value: Int): T = {
    triggerBuilder.usingJobData(dataKey, new java.lang.Integer(value))
    this.asInstanceOf[T]
  }

  def triggerUsingJobData(dataKey: String, value: Long): T = {
    triggerBuilder.usingJobData(dataKey, new java.lang.Long(value))
    this.asInstanceOf[T]
  }

  def triggerUsingJobData(dataKey: String, value: String): T = {
    triggerBuilder.usingJobData(dataKey, value)
    this.asInstanceOf[T]
  }

  def triggerWithDescription(triggerDescription: String): T = {
    triggerBuilder.withDescription(triggerDescription)
    this.asInstanceOf[T]
  }

  def triggerWithIdentity(name: String): T = {
    triggerBuilder.withIdentity(name)
    this.asInstanceOf[T]
  }

  def triggerWithIdentity(name: String, group: String): T = {
    triggerBuilder.withIdentity(name, group)
    this.asInstanceOf[T]
  }

  def triggerWithIdentity(triggerKey: TriggerKey): T = {
    triggerBuilder.withIdentity(triggerKey)
    this.asInstanceOf[T]
  }

  def triggerWithPriority(triggerPriority: Int): T = {
    triggerBuilder.withPriority(triggerPriority)
    this.asInstanceOf[T]
  }

  protected def getScheduleBuilder: ScheduleBuilder[_ <: Trigger]

  def sched: (Date, Trigger, Option[JobDetail]) = {
    triggerBuilder.withSchedule(getScheduleBuilder)
    val trigger = triggerBuilder.build
    jobBuilderOpt match {
      case Some(jobBuilder) =>
        val jobDetail = jobBuilder.build
        (squartz.sched(jobDetail, trigger), trigger, Some(jobDetail))
      case None =>
        (squartz.sched(trigger), trigger, None)
    }
  }
}

object SquartzCronBuilder {

  def build[A <: Job](cronStr: String)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzCronBuilder(CronScheduleBuilder.cronSchedule(cronStr))

  def dailyAtHourAndMinute(hour: Int, minute: Int)(implicit squartz: Squartz) = new SquartzCronBuilder[NoJob](
    CronScheduleBuilder.dailyAtHourAndMinute(hour, minute)
  )

  def dailyAtHourAndMinute[A <: Job]
  (
    hour: Int, minute: Int
  )(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzCronBuilder[A](
    CronScheduleBuilder.dailyAtHourAndMinute(hour, minute)
  )

  def monthlyOnDayAndHourAndMinute(dayOfMonth: Int, hour: Int, minute: Int)(implicit squartz: Squartz) = new SquartzCronBuilder[NoJob](
    CronScheduleBuilder.monthlyOnDayAndHourAndMinute(dayOfMonth, hour, minute)
  )

  def monthlyOnDayAndHourAndMinute[A <: Job]
  (
    dayOfMonth: Int, hour: Int, minute: Int
  )(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzCronBuilder[A](
    CronScheduleBuilder.monthlyOnDayAndHourAndMinute(dayOfMonth, hour, minute)
  )

  def weeklyOnDayAndHourAndMinute(dayOfWeek: Int, hour: Int, minute: Int)(implicit squartz: Squartz) = new SquartzCronBuilder[NoJob](
    CronScheduleBuilder.weeklyOnDayAndHourAndMinute(dayOfWeek, hour, minute)
  )

  def weeklyOnDayAndHourAndMinute[A <: Job]
  (
    dayOfWeek: Int, hour: Int, minute: Int
  )(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzCronBuilder[A](
    CronScheduleBuilder.weeklyOnDayAndHourAndMinute(dayOfWeek, hour, minute)
  )
}

class SquartzCronBuilder[A <: Job]
(
  scheduleBuilder: CronScheduleBuilder
)(implicit squartz: Squartz, mA: ClassTag[A]) extends SquartzBuilder[SquartzCronBuilder[A], A](squartz) {

  def scheduleInTimeZone(timezone: java.util.TimeZone): SquartzCronBuilder[A] = {
    scheduleBuilder.inTimeZone(timezone)
    this
  }

  def scheduleWithMisfireHandlingInstructionDoNothing: SquartzCronBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionDoNothing
    this
  }

  def scheduleWithMisfireHandlingInstructionFireAndProceed: SquartzCronBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionFireAndProceed
    this
  }

  def scheduleWithMisfireHandlingInstructionIgnoreMisfires: SquartzCronBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires
    this
  }

  override protected def getScheduleBuilder = scheduleBuilder
}

object SquartzSimpleBuilder {

  def build[A <: Job]()(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](SimpleScheduleBuilder.simpleSchedule)

  def repeatHourlyForever()(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatHourlyForever
  )

  def repeatHourlyForever[A <: Job](implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatHourlyForever
  )

  def repeatHourlyForever(hours: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder(
    SimpleScheduleBuilder.repeatHourlyForever(hours)
  )

  def repeatHourlyForever[A <: Job](hours: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatHourlyForever(hours)
  )

  def repeatHourlyForTotalCount(count: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatHourlyForTotalCount(count)
  )

  def repeatHourlyForTotalCount[A <: Job](count: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatHourlyForTotalCount(count)
  )

  def repeatHourlyForTotalCount(count: Int, hours: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatHourlyForTotalCount(count, hours)
  )

  def repeatHourlyForTotalCount[A <: Job](count: Int, hours: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatHourlyForTotalCount(count, hours)
  )

  def repeatMinutelyForever()(implicit squartz: Squartz) = new SquartzSimpleBuilder(
    SimpleScheduleBuilder.repeatMinutelyForever
  )

  def repeatMinutelyForever[A <: Job]()(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatMinutelyForever
  )

  def repeatMinutelyForever(minutes: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder(
    SimpleScheduleBuilder.repeatMinutelyForever(minutes)
  )

  def repeatMinutelyForever[A <: Job](minutes: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatMinutelyForever(minutes)
  )

  def repeatMinutelyForTotalCount(count: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatMinutelyForTotalCount(count)
  )

  def repeatMinutelyForTotalCount[A <: Job](count: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatMinutelyForTotalCount(count)
  )

  def repeatMinutelyForTotalCount(count: Int, minutes: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatMinutelyForTotalCount(count, minutes)
  )

  def repeatMinutelyForTotalCount[A <: Job](count: Int, minutes: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatMinutelyForTotalCount(count, minutes)
  )

  def repeatSecondlyForever()(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatSecondlyForever
  )

  def repeatSecondlyForever[A <: Job]()(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatSecondlyForever
  )

  def repeatSecondlyForever(seconds: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatSecondlyForever(seconds)
  )

  def repeatSecondlyForever[A <: Job](seconds: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatSecondlyForever(seconds)
  )

  def repeatSecondlyForTotalCount(count: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatSecondlyForTotalCount(count)
  )

  def repeatSecondlyForTotalCount[A <: Job](count: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatSecondlyForTotalCount(count)
  )

  def repeatSecondlyForTotalCount(count: Int, seconds: Int)(implicit squartz: Squartz) = new SquartzSimpleBuilder[NoJob](
    SimpleScheduleBuilder.repeatSecondlyForTotalCount(count, seconds)
  )

  def repeatSecondlyForTotalCount[A <: Job](count: Int, seconds: Int)(implicit squartz: Squartz, mA: ClassTag[A]) = new SquartzSimpleBuilder[A](
    SimpleScheduleBuilder.repeatSecondlyForTotalCount(count, seconds)
  )
}

class SquartzSimpleBuilder[A <: Job]
(
  scheduleBuilder: SimpleScheduleBuilder
)(implicit squartz: Squartz, mA: ClassTag[A]) extends SquartzBuilder[SquartzSimpleBuilder[A], A](squartz) {

  def scheduleRepeatForever: SquartzSimpleBuilder[A] = {
    scheduleBuilder.repeatForever
    this
  }

  def scheduleWithIntervalInHours(intervalHours: Int): SquartzSimpleBuilder[A] = {
    scheduleBuilder.withIntervalInHours(intervalHours)
    this
  }

  def scheduleWithIntervalInMilliseconds(intervalInMillis: Long): SquartzSimpleBuilder[A] = {
    scheduleBuilder.withIntervalInMilliseconds(intervalInMillis)
    this
  }

  def scheduleWithIntervalInMinutes(intervalInMinutes: Int): SquartzSimpleBuilder[A] = {
    scheduleBuilder.withIntervalInMinutes(intervalInMinutes)
    this
  }

  def scheduleWithIntervalInSeconds(intervalInSeconds: Int): SquartzSimpleBuilder[A] = {
    scheduleBuilder.withIntervalInSeconds(intervalInSeconds)
    this
  }

  def scheduleWithMisfireHandlingInstructionFireNow: SquartzSimpleBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionFireNow
    this
  }

  def scheduleWithMisfireHandlingInstructionIgnoreMisfires: SquartzSimpleBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires
    this
  }

  def scheduleWithMisfireHandlingInstructionNextWithExistingCount: SquartzSimpleBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount
    this
  }

  def scheduleWithMisfireHandlingInstructionNextWithRemainingCount: SquartzSimpleBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount
    this
  }

  def scheduleWithMisfireHandlingInstructionNowWithExistingCount: SquartzSimpleBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionNowWithExistingCount
    this
  }

  def scheduleWithMisfireHandlingInstructionNowWithRemainingCount: SquartzSimpleBuilder[A] = {
    scheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount
    this
  }

  def scheduleWithRepeatCount(triggerRepeatCount: Int): SquartzSimpleBuilder[A] = {
    scheduleBuilder.withRepeatCount(triggerRepeatCount)
    this
  }

  override protected def getScheduleBuilder = scheduleBuilder
}
