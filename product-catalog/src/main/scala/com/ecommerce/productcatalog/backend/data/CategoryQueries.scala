package com.ecommerce.productcatalog.backend.data

import java.util.UUID
import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/24/17.
  */
trait CategoryQueries {

  private def tableQuery = TableQuery[CategoryTable]

  def getAll = tableQuery.map(toCategory)

  def getById(categoryId: UUID) = tableQuery.filter(_.categoryId === categoryId).map(toCategory)

  def toCategory(r: CategoryTable) = (r.categoryId, r.categoryName) <> (Category.tupled, Category.unapply)
}

