package com.ecommerce.inventory.backend

import Backorder._

/**
  * Created by lukewyman on 12/21/16.
  */
trait BackorderMessage {
  def inventoryItem: ItemRef
}

object BackorderMessage {

  sealed trait Command extends BackorderMessage
  case class SetProduct(inventoryItem: ItemRef) extends Command
  case class AcknowledgeShipment(inventoryItem: ItemRef, shipment: ShipmentRef) extends Command
  case class AcceptShipment(inventoryItem: ItemRef, shipment: ShipmentRef) extends Command
  case class MakeReservation(inventoryItem: ItemRef, reservation: Reservation, count: Int) extends Command
  case class AbandonCart(inventoryItem: ItemRef,shoppingCart: ShoppingCartRef) extends Command

  sealed trait Event extends BackorderMessage
  case class ProductChanged(inventoryItem: ItemRef) extends Event
  case class ShipmentAcknowledged(inventoryItem: ItemRef, shipment: ShipmentRef) extends Event
  case class ShipmentAccepted(inventoryItem: ItemRef, shipment: ShipmentRef) extends Event
  case class ReservationMade(inventoryItem: ItemRef, reservation: Reservation, count: Int) extends Event
  case class CartAbandoned(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef) extends Event

  sealed trait Query extends BackorderMessage
  case class GetBackorder(inventoryItem: ItemRef) extends Query

  sealed trait Result extends BackorderMessage
  case class GetBackorderResult(inventoryItem: ItemRef) extends Result
}
