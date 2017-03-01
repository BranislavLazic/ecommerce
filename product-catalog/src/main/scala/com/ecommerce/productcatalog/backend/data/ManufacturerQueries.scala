package com.ecommerce.productcatalog.backend.data

import java.util.UUID
import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/26/17.
  */
trait ManufacturerQueries extends Database {

  private def tableQuery = TableQuery[ManufacturerTable]

  def getAll = {
    val query = tableQuery.map(toManufacturer)
    db.run(query.result)
  }

  def getById(manufacturerId: UUID) = {
    val query = tableQuery.filter(_.manufacturerId === manufacturerId).map(toManufacturer)
    db.run(query.result.head)
  }

  private def toManufacturer(r: ManufacturerTable) =
    (r.manufacturerId, r.manufacturerName) <> (Manufacturer.tupled, Manufacturer.unapply)

}
