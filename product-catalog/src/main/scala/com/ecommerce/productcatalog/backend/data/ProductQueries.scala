package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/26/17.
  */

trait ProductQueries  {

  private val products = TableQuery[ProductTable]
  private val categories = TableQuery[CategoryTable]
  private val manufacturers = TableQuery[ManufacturerTable]

  def getProductById(productId: UUID) = {
    val query = for {
      p <- products if p.productId === productId
      c <- categories if p.categoryId === c.categoryId
      m <- manufacturers if p.manufacturerId === m.manufacturerId
    } yield (p, c, m)
    (query.map {
      case (p, c, m) =>
        val category = (c.categoryId, c.categoryName) <> (Category.tupled, Category.unapply)
        val manufacturer = (m.manufacturerId, m.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)
        (p.productId, p.productCode, p.displayName, p.description, p.price, category, manufacturer) <> (Product.tupled, Product.unapply)
    }).result.head
  }

  def toProduct: PartialFunction[(ProductTable, CategoryTable, ManufacturerTable), Rep[Product]] = {
    case (p, c, m) =>
      val category = (c.categoryId, c.categoryName) <> (Category.tupled, Category.unapply)
      val manufacturer = (m.manufacturerId, m.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)
      (p.productId, p.productCode, p.displayName, p.description, p.price, category, manufacturer) <> (Product.tupled, Product.unapply)
  }
}

