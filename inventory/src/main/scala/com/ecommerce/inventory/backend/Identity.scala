package com.ecommerce.inventory.backend

import java.util.UUID

import org.joda.time.DateTime

/**
  * Created by lukewyman on 12/23/16.
  */
object Identity {

  case class ItemRef(id: UUID)
  case class ShipmentRef(id: UUID, expectedDate: DateTime, count: Int)
  case class ShoppingCartRef(id: UUID)
  case class CustomerRef(id: UUID)
  case class Reservation(customer: CustomerRef, shipmentRef: ShipmentRef)
}
