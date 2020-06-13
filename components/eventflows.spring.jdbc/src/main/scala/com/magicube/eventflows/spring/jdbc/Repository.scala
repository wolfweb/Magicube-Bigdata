package com.magicube.eventflows.spring.jdbc

import com.magicube.eventflows.Repository.IEntityBase
import org.springframework.data.jdbc.core.JdbcAggregateTemplate
import org.springframework.stereotype.Service

@Service
case class Repository[T <: IEntityBase[TKey], TKey](operations: JdbcAggregateTemplate) {

}

