package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/26/17.
  */

trait ProductQueries extends Database {
//TODO: Waaaaaaay too much code duplication in this trait. Need to come back and refactor

  private val products = TableQuery[ProductTable]
  private val categories = TableQuery[CategoryTable]
  private val manufacturers = TableQuery[ManufacturerTable]

  def getProductById(productId: UUID) = {
    val query = for {
      p <- products if p.productId === productId
      c <- categories if p.categoryId === c.categoryId
      m <- manufacturers if p.manufacturerId === m.manufacturerId
    } yield (p, c, m)
    val projectedQuery = query.map {
      case (p, c, m) =>
        val category = (c.categoryId, c.categoryName) <> (Category.tupled, Category.unapply)
        val manufacturer = (m.manufacturerId, m.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)
        (p.productId, p.productCode, p.displayName, p.description, p.price, category, manufacturer) <> (Product.tupled, Product.unapply)
    }
    db.run(projectedQuery.result.head)
  }

  def getProductsByCategory(categoryId: UUID) = {
    val query = for {
      p <- products if p.categoryId === categoryId
      c <- categories if p.categoryId === c.categoryId
      m <- manufacturers if p.manufacturerId === m.manufacturerId
    } yield (p, c, m)
    val projectedQuery = query.map {
      case (p, c, m) =>
        val category = (c.categoryId, c.categoryName) <> (Category.tupled, Category.unapply)
        val manufacturer = (m.manufacturerId, m.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)
        (p.productId, p.productCode, p.displayName, p.description, p.price, category, manufacturer) <> (Product.tupled, Product.unapply)
    }
    db.run(projectedQuery.result)
  }

  def getProductsByManufacturer(manufacturerId: UUID) = {
    val query = for {
      p <- products if p.manufacturerId === manufacturerId
      c <- categories if p.categoryId === c.categoryId
      m <- manufacturers if p.manufacturerId === m.manufacturerId
    } yield (p, c, m)
    val projectedQuery = query.map {
      case (p, c, m) =>
        val category = (c.categoryId, c.categoryName) <> (Category.tupled, Category.unapply)
        val manufacturer = (m.manufacturerId, m.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)
        (p.productId, p.productCode, p.displayName, p.description, p.price, category, manufacturer) <> (Product.tupled, Product.unapply)
    }
    db.run(projectedQuery.result)
  }

  // TODO: just an outline - very sloppy and won't work as is
  def getProductsBySearchString(categoryId: Option[UUID], searchString: String) = {
    val query = for {
      p <- products if p.displayName like searchString
      c <- categories if p.categoryId === c.categoryId
      m <- manufacturers if p.manufacturerId === m.manufacturerId
    } yield (p, c, m)
    val projectedQuery = query.map {
      case (p, c, m) =>
        val category = (c.categoryId, c.categoryName) <> (Category.tupled, Category.unapply)
        val manufacturer = (m.manufacturerId, m.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)
        (p.productId, p.productCode, p.displayName, p.description, p.price, category, manufacturer) <> (Product.tupled, Product.unapply)
    }
    db.run(projectedQuery.result)
  }

  // TODO: Figure out how to use this partial function in the query methods above, without a compile error
  def toProduct: PartialFunction[(ProductTable, CategoryTable, ManufacturerTable), Rep[Product]] = {
    case (p, c, m) =>
      val category = (c.categoryId, c.categoryName) <> (Category.tupled, Category.unapply)
      val manufacturer = (m.manufacturerId, m.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)
      (p.productId, p.productCode, p.displayName, p.description, p.price, category, manufacturer) <> (Product.tupled, Product.unapply)
  }
}

