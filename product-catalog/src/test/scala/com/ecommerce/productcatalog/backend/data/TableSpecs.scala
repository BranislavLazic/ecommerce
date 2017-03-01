package com.ecommerce.productcatalog.backend.data

import java.util.UUID
import com.ecommerce.productcatalog.backend.DbTestBase
import org.scalatest.{FlatSpecLike, Matchers}
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lukewyman on 2/27/17.
  */
class TableSpecs extends FlatSpecLike with Matchers {

  val categories = TableQuery[CategoryTable]
  val manufacturers = TableQuery[ManufacturerTable]
  val products = TableQuery[ProductTable]

  "A CategoryTable" should "insert and select categories" in {

    val catId1 = UUID.randomUUID()
    val catId2 = UUID.randomUUID()

    val insert = DBIO.seq(
      categories += (catId1, "cat1"),
      categories += (catId2, "cat2")
    )

    Await.ready(DbTestBase.db.run(insert), Duration.Inf)

    val result = Await.result(DbTestBase.db.run(categories.result), Duration.Inf)

    result should contain (catId1, "cat1")
    result should contain (catId2, "cat2")
  }

  "A ManufacturerTable " should "insert and select manufacturers" in {

    val manId1 = UUID.randomUUID()
    val manId2 = UUID.randomUUID()

    val insert = DBIO.seq(
      manufacturers += (manId1, "man1"),
      manufacturers += (manId2, "man2")
    )

    Await.ready(DbTestBase.db.run(insert), Duration.Inf)

    val result = Await.result(DbTestBase.db.run(manufacturers.result), Duration.Inf)

    result should contain (manId1, "man1")
    result should contain (manId2, "man2")
  }

  "A ProductTable" should "insert and select products" in {

    val prodId1 = UUID.randomUUID()
    val catIdProd1 = UUID.randomUUID()
    val manIdProd1 = UUID.randomUUID()
    val prodId2 = UUID.randomUUID()
    val catIdProd2 = UUID.randomUUID()
    val manIdProd2 = UUID.randomUUID()

    val insertFK = DBIO.seq(
      categories += (catIdProd1, "catProd1"),
      categories += (catIdProd2, "catProd2"),
      manufacturers += (manIdProd1, "manProd1"),
      manufacturers += (manIdProd2, "manProd2")
    )

    Await.ready(DbTestBase.db.run(insertFK), Duration.Inf)

    val insert = DBIO.seq(
      products += (prodId1, catIdProd1, manIdProd1, "code1", "prod1", "desc1", 9.99),
      products += (prodId2, catIdProd2, manIdProd2, "code2", "prod2", "desc2", 14.99)
    )

    Await.ready(DbTestBase.db.run(insert), Duration.Inf)

    val result = Await.result(DbTestBase.db.run(products.result), Duration.Inf)

    result should contain (prodId1, catIdProd1, manIdProd1, "code1", "prod1", "desc1", 9.99)
    result should contain (prodId2, catIdProd2, manIdProd2, "code2", "prod2", "desc2", 14.99)
  }

}
