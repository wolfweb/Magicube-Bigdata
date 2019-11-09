package com.magicube.eventflows.Repository.Mongo

import com.magicube.eventflows.Repository.Entity
import com.mongodb._
import com.mongodb.client.MongoDatabase
import com.mysema.commons.lang.Assert

abstract class MongoRepository[T <: Entity[T, Id], Id](uri: String) {
  private val mongoDbFactorySupport = MongoDbFactorySupport.buildMongodb(uri)

  protected def doGetDatabase(dbName: String): MongoDatabase = if (dbName == null) mongoDbFactorySupport.getDb else mongoDbFactorySupport.getDb(dbName)

  protected def prepareDatabase(database: MongoDatabase): MongoDatabase = database
}

trait DbCallback[T <: Entity[T, Id], Id] {
  @throws[MongoException]
  def doInDB(db: MongoDatabase): T
}