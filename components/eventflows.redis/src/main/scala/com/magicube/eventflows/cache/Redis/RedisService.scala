package com.magicube.eventflows.cache.Redis

import com.magicube.eventflows.Json.JSON._
import org.json4s.DefaultFormats
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis

import scala.collection.JavaConverters._


case class RedisService(conf: RedisConf) {
  protected val logger = LoggerFactory.getLogger(getClass.getName)

  private val _client = new Jedis(conf.host, conf.port)

  //30 hour
  val defExpire = 108000

  def del(key: String): Unit = {
    _client.del(key)
    logger.debug(s"delete key $key")
  }

  def get(key: String): String = {
    val v = _client.get(key)
    logger.debug(s"get key $key with value $v")
    v
  }

  def getAs[T: Manifest](key: String): T = {
    val v = get(key)
    if (v != null)
      deserialize(v)
    else
      null.asInstanceOf[T]
  }

  def getHashSize(key: String): Long = _client.hlen(key)

  def getIncr(key: String): Long = {
    val v = get(key)
    if (v == null || v.isEmpty)
      0
    else
      v.toLong
  }

  def getListSize(key: String): Long = _client.llen(key)

  def hasKey(key: String): Boolean = _client.exists(key)

  def hashExist(key: String, field: String): Boolean = _client.hexists(key, field)

  def hashGet[T: Manifest](key: String, field: String): T = {
    if (_client.hexists(key, field)) {
      val v = _client.hget(key, field)
      deserialize[T](v)
    } else
      null.asInstanceOf[T]
  }

  def hashGetAll[T: Manifest](key: String): Array[T] = {
    if (_client.exists(key))
      _client.hgetAll(key).asScala.map(x => deserialize[T](x._2)).toArray
    else
      null.asInstanceOf[Array[T]]
  }

  def hashIncr(key: String, field: String, value: Long = 1, expired: Int = defExpire): Long = {
    val v = _client.hincrBy(key, field, value)
    setExpired(key, expired)
    v
  }

  def hashPop[T: Manifest](key: String, field: String): T = {
    if (_client.hexists(key, field)) {
      val v = _client.hget(key, field)
      _client.hdel(key, field)
      deserialize[T](v)
    } else
      null.asInstanceOf[T]
  }

  def hashRemove(key: String, field: String): Unit = {
    _client.hdel(key, field)
  }

  def hashSet[T: Manifest](key: String, field: String, value: T, expired: Int = defExpire): Unit = {
    _client.hset(key, field, serialize(value))
    setExpired(key, expired)
  }

  def incrSet(key: String, value: Long = 1, expired: Int = defExpire): Unit = {
    _client.incrBy(key, value)
    setExpired(key, expired)
  }

  def listGetAll[T: Manifest](key: String): Array[T] = {
    if (_client.exists(key))
      _client.lrange(key, 0, -1).asScala.map(x => deserialize(x)).toArray
    else
      null.asInstanceOf[Array[T]]
  }

  def listPop[T: Manifest](key: String): T = {
    val v = _client.rpop(key)
    if (v != null)
      deserialize(v)
    else
      null.asInstanceOf[T]
  }

  def listSet[T: Manifest](key: String, value: T, expired: Int = defExpire): Unit = {
    val v = serialize(value)
    _client.lpush(key, v)
    setExpired(key, expired)
  }

  def set(key: String, value: String, expired: Int = defExpire): Unit = {
    _client.set(key, value)
    setExpired(key, expired)
  }

  def setAs[T: Manifest](key: String, value: T, expired: Int = defExpire): Unit = {
    val v = serialize(value)
    set(key, v, expired)
  }

  def sortedSet(key: String, score: Double, member: String, expired: Int = defExpire): Unit = {
    _client.zincrby(key, score, member)
    setExpired(key, expired)
  }

  def sortedGet(key: String, start: Int = 0, end: Int = -1): Map[Any, Any] = {
    if (_client.exists(key)) {
      val datas = _client.zrevrangeByScoreWithScores(key, 1000000, -20000, start, end)
      datas.asScala.zipWithIndex.map(x => x._1.getElement -> x._1.getScore).toMap
    } else {
      null.asInstanceOf[Map[Any, Any]]
    }
  }

  private def setExpired(key: String, expired: Int): Unit = {
    _client.expire(key, expired)
  }
}
