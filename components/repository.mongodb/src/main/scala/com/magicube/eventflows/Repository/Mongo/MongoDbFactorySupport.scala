package com.magicube.eventflows.Repository.Mongo

import com.mongodb.client.MongoDatabase
import com.mongodb.{MongoClient, MongoClientURI, WriteConcern}
import com.mysema.commons.lang.Assert

object MongoDbFactorySupport {
  private var mongodbCache = Map[String, MongoDbFactorySupport]()

  def buildMongodb(uri: String): MongoDbFactorySupport = {
    val mongoUri = new MongoClientURI(uri)
    var mongodb: Option[(String, MongoDbFactorySupport)] = mongodbCache.find(x => x._1 == mongoUri.getURI)
    if (mongodb.isEmpty) {
      val db = new MongoDbFactorySupport(new MongoClient(mongoUri), mongoUri.getDatabase)
      mongodbCache += uri -> db
      mongodb = Some(uri, db)
    }
    mongodb.get._2
  }
}

class MongoDbFactorySupport(client: MongoClient, dbName: String) {
  private val mongoClient = client
  private val databaseName = dbName
  private var writeConcern: WriteConcern = _

  def getDb: MongoDatabase = getDb(databaseName)

  def getDb(dbName: String): MongoDatabase = {
    Assert.hasText(dbName, "Database name must not be empty!")
    val db = doGetMongoDatabase(dbName)
    if (writeConcern == null) return db
    db.withWriteConcern(writeConcern)
  }

  def setWriteConcern(writeConcern: WriteConcern): Unit = {
    this.writeConcern = writeConcern
  }

  def doGetMongoDatabase(dbName: String): MongoDatabase = getMongoClient.getDatabase(dbName)

  def closeClient(): Unit = getMongoClient.close()

  protected def getDefaultDatabaseName: String = databaseName

  protected def getMongoClient: MongoClient = mongoClient
}
