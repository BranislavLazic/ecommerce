package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/25/17.
  */

private[data] class CategoryTable(tag: Tag) extends Table[(UUID, String)](tag, "category") {

  def categoryId = column[UUID]("categoryid", O.PrimaryKey)
  def categoryName = column[String]("categoryname")

  def * = (categoryId, categoryName)
}
