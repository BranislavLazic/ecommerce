package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import com.ecommerce.productcatalog.backend.DbTestBase
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import slick.lifted.TableQuery
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by lukewyman on 2/28/17.
  */
class ManufacturerQueriesSpec extends FlatSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val manufacterers = TableQuery[ManufacturerTable]

  val manId1 = UUID.randomUUID()
  val manId2 = UUID.randomUUID()

  override def beforeAll(): Unit = {

    val insert = DBIO.seq(
      manufacterers += (manId1, "manQ1"),
      manufacterers += (manId2, "manQ2")
    )

    Await.ready(DbTestBase.db.run(insert), Duration.Inf)
  }

  "ManufacturerQueries" should "select a manufacturer by Id" in {

    val manufacturerQueries = new ManufacturerQueries {}
    val result = Await.result(manufacturerQueries.getById(manId1), Duration.Inf)

    result should be (Manufacturer(manId1, "manQ1"))
  }

  "ManufactuerQueries" should "select all categories" in {

    val manufacturerQueries = new ManufacturerQueries {}
    val result = Await.result(manufacturerQueries.getAll, Duration.Inf)

    result should contain (Manufacturer(manId1, "manQ1"))
    result should contain (Manufacturer(manId2, "manQ2"))
  }
}
