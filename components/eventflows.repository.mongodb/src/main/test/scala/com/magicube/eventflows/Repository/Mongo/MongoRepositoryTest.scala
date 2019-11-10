package com.magicube.eventflows.Repository.Mongo

import com.magicube.eventflows.Awaiting
import org.junit.Test
import reactivemongo.bson._

class MongoRepositoryTest extends  Awaiting  {
  import User._
  import com.magicube.eventflows.Repository.Mongo.Criteria.Term
  import com.magicube.eventflows.Repository.Mongo.Criteria.Typed._

  val url = "mongodb://localhost:27017/scala-debug"
  val database = MongoConnector(url)

  @Test
  def Func_UserMongoRepo_Test(): Unit = {
    val rep = UserRepository(database.get)
    await(rep.insert(User(1,23,"wolfweb",  List[Subject](Subject("语文",88)))))

    val proj: BSONDocument = criteria[User](_.scores).elemMatch(criteria[Subject](_.name) === "数学")
    var model = await(rep.findById(BSONInteger(1), Some(proj)))
    if (model != None) {
      val scores = model.get.getAs[List[Subject]]("scores")
      scores match {
        case None => {
          await(rep.updateById(BSONInteger(1), $push("scores", Subject("数学", 100))))
        }
        case Some(x) => {
          if (x.head.score == 99) {
            await(rep.updateById(BSONInteger(1), $push("scores", Subject("英语", 88))))
          } else {
            val filter = Term("_id") === 1 && criteria[User](_.scores).elemMatch(criteria[Subject](_.name) === "数学")
            await(rep.updateBy(filter, $set("scores.$.score", BSONInteger(99))))
          }
        }
      }
    }
  }

  case class UserRepository(database: MongoDB) extends Repository[User, BSONInteger](
    database,
    "User",
    userReader,
    userWriter
  ) {

  }
}
