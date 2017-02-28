package com.ecommerce.productcatalog.backend.data

import java.util.UUID
import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/24/17.
  */
trait CategoryQueries extends Database {

  private def tableQuery = TableQuery[CategoryTable]

  def getAll = {
    val query = tableQuery.map(toCategory)
    db.run(query.result)
  }

  def getById(categoryId: UUID) = {
    val query = tableQuery.filter(_.categoryId === categoryId).map(toCategory)
    db.run(query.result.head)
  }

  private def toCategory(r: CategoryTable) =
    (r.categoryId, r.categoryName) <> (Category.tupled, Category.unapply)
}

