package com.magicube.eventflows.Repository.Mongo

import com.magicube.eventflows.Repository.Mongo.Criteria.Term
import com.magicube.eventflows.Repository.Mongo.Criteria.Typed._
import org.junit.Test
import reactivemongo.bson.{BSONDocument, _}

class TypedCriteriaTest {
  @Test
  def func_test(): Unit = {
    assert(BSONDocument.pretty(criteria[User](_.name) =/= "a value") == BSONDocument.pretty(BSONDocument("name" -> BSONDocument("$ne" -> BSONString("a value")))))
    assert(BSONDocument.pretty(criteria[User](_.age) @== 99) == BSONDocument.pretty(BSONDocument("age" -> BSONInteger(99))))

    val r = BSONDocument.pretty(Term("_id") === 1 && criteria[User](_.scores).elemMatch(criteria[Subject](_.name) === "数学"))
    val s = BSONDocument.pretty($set("scores", BSONDocument("scores.$.score"->99)).toDocument)

    println(r)
    println(s)
  }
}


