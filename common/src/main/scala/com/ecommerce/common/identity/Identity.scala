package com.ecommerce.common.identity

import java.util.UUID

/**
  * Created by lukewyman on 2/16/17.
  */

object Identity {

  sealed trait Id

  case class CategoryRef(id: UUID) extends Id

  case class CustomerRef(id: UUID) extends Id

  case class ManufacturerRef(id: UUID) extends Id

  case class PaymentRef(id: UUID) extends Id

  case class ProductRef(id: UUID) extends Id

  case class ReservationRef(customer: CustomerRef, shipmentRef: ShipmentRef) extends Id

  case class ShipmentRef(id: UUID) extends Id

  case class ShoppingCartRef(id: UUID) extends Id

}