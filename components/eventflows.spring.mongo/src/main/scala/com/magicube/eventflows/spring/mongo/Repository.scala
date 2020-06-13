package com.magicube.eventflows.spring.mongo

import com.magicube.eventflows.Repository.IEntityBase
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.stereotype.Service

@Service
case class Repository[T <: IEntityBase[TKey], TKey](operations: MongoOperations) {

}
