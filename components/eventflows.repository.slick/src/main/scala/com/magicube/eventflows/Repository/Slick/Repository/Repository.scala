package com.magicube.eventflows.Repository.Slick.Repository

import com.magicube.eventflows.Repository.Slick.Conf.Config
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class Repository[T <: Entity[T, Id], Id](config: Config) extends BaseRepository[T, Id] {
  val logger = LoggerFactory.getLogger("Repository")

  val driver: JdbcProfile = config.driver

  import driver.api._

  val db = Database.forURL(
    url = config.conf.url,
    driver = config.conf.driver,
    user = config.conf.user,
    password = config.conf.password
  )

  val initialize = init()

  def executeAction[X](action: DBIOAction[X, NoStream, _]): X = {
    Await.result(db.run(action), Duration.Inf)
  }

  private def init() = {
    waitInitialized()
    executeAction(tableQuery.schema.createIfNotExists)
    true
  }

  private def waitInitialized(): Unit = {
    val attempts = 20
    val sleep = 3000L
    var initialized = false
    (1 to attempts).foreach(
      i =>
        try {
          if (!initialized) {
            val query = config.validationQuery
            executeAction(sql"#$query".as[(Int)].headOption)
            logger.info("Connected to database " + config.name)
            initialized = true
          }
        } catch {
          case e: Exception =>
            val message = if (i < attempts) "Will wait " + sleep + " ms before retry" else "Giving up"
            logger.warn("Could not connect to database " + config.name + " [attempt " + i + "]. " + message)
            if (i < attempts) {
              Thread.sleep(sleep)
            }
        }
    )
  }
}
