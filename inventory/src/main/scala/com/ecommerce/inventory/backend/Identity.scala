package com.ecommerce.inventory.backend

import java.util.UUID

import org.joda.time.DateTime

/**
  * Created by lukewyman on 12/23/16.
  */
object Identity {

  sealed trait Id
  case class ItemRef(id: UUID) extends Id
  case class ShipmentRef(id: UUID, expectedDate: DateTime, count: Int) extends Id
  case class ShoppingCartRef(id: UUID) extends Id
  case class CustomerRef(id: UUID) extends Id
  case class Reservation(customer: CustomerRef, shipmentRef: ShipmentRef)
  case class PaymentRef(id: UUID) extends Id
}
