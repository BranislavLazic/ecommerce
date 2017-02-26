package com.ecommerce.productcatalog.backend.data

import java.util.UUID

import slick.jdbc.MySQLProfile.api._

/**
  * Created by lukewyman on 2/25/17.
  */
private[data] class ManufacturerTable(tag: Tag) extends Table[(UUID, String)](tag, "manufacturer") {

  def manufacturerId = column[UUID]("manufacturerid", O.PrimaryKey)
  def manufacturerName = column[String]("manufacturername")

  def * = (manufacturerId, manufacturerName)
}
