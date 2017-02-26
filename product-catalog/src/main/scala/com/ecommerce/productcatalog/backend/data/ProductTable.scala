package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/25/17.
  */
private[data] class ProductTable(tag: Tag) extends Table[(UUID, UUID, UUID, String, String, String, Double)](tag, "product") {

  def productId = column[UUID]("productid")
  def categoryId = column[UUID]("categoryid")
  def manufacturerId = column[UUID]("manufacturerid")
  def productCode = column[String]("productcode")
  def displayName = column[String]("displayname")
  def description = column[String]("description")
  def price = column[Double]("price")

  def * = (productId, categoryId, manufacturerId, productCode, displayName, description, price)
}
