package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import com.ecommerce.productcatalog.backend.DbTestBase
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpecLike}
import slick.lifted.TableQuery

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/28/17.
  */
class CategoryQueriesSpec extends FlatSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val categories = TableQuery[CategoryTable]

  val catId1 = UUID.randomUUID()
  val catId2 = UUID.randomUUID()

  override def beforeAll(): Unit = {

    val insert = DBIO.seq(
      categories += (catId1, "catQ1"),
      categories += (catId2, "catQ2")
    )

    Await.ready(DbTestBase.db.run(insert), Duration.Inf)
  }

  "CategoryQueries" should "select a category by Id" in {

    val categoryQueries = new CategoryQueries {}
    val result = Await.result(categoryQueries.getById(catId1), Duration.Inf)

    result should be (Category(catId1, "catQ1"))
  }

  "CategoryQueries" should "select all categories" in {

    val categoryQueries = new CategoryQueries {}
    val result = Await.result(categoryQueries.getAll, Duration.Inf)

    result should contain (Category(catId1, "catQ1"))
    result should contain (Category(catId2, "catQ2"))
  }
}
