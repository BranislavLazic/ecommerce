package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import slick.jdbc.MySQLProfile.api._
/**
  * Created by lukewyman on 2/26/17.
  */
trait ManufacturerQueries {

  def getAll = TableQuery[ManufacturerTable]

  def getById(manufacturerId: UUID) = getAll.filter(_.manufacturerId === manufacturerId)

}
