package com.magicube.eventflows.spring.mongo

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration

@Configuration
abstract class MongoConfiguration() extends AbstractMongoClientConfiguration {
  val conf: MongoConf

  override def autoIndexCreation(): Boolean = true

  override def getDatabaseName: String = conf.database

  override def mongoClient(): MongoClient = MongoClients.create(s"mongodb://${conf.host}:${conf.port}")
}

case class MongoConf
(
  host: String,
  port: Int,
  database: String
)