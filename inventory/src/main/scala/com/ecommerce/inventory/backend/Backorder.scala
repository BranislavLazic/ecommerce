package com.ecommerce.inventory.backend

import java.util.UUID
import Identity._
import com.ecommerce.inventory.backend.BackorderMessage._
import org.joda.time.DateTime

/**
  * Created by lukewyman on 12/18/16.
  */
case class Backorder(product: Option[ItemRef], expectedShipments: List[ShipmentRef], reservations: Map[Reservation, Int]) {

  def setProduct(item: ItemRef): Backorder = {
    require(product.isEmpty, "product cannot be overwritten")
    copy(product = Some(item))
  }

  def acknowledgeShipment(shipment: ShipmentRef): Backorder = {
    copy(expectedShipments = shipment :: expectedShipments)
  }

  def acceptShipment(shipment: ShipmentRef): Backorder = ???

  def makeReservation(reservation: Reservation, count: Int): Backorder = {
    copy(reservations = reservations.updated(reservation, count))
  }

  def abandonCart(shoppingCart: ShoppingCartRef): Backorder = ???

  def count: Int = {
    expectedShipments.map(_.count).sum - reservations.values.sum
  }

  def availableCount(date: DateTime): Int = {
    val reservationCount = reservations.filter({ case (r, c) => r.shipmentRef.expectedDate == date }).values.sum
    expectedShipments.filter(_.expectedDate == date).map(_.count).sum - reservationCount
  }

  def applyEvent(event: Event) = event match {
    case ProductChanged(i) => setProduct(i)
    case ShipmentAcknowledged(_, s) => acknowledgeShipment(s)
    case ShipmentAccepted(_, s) => acceptShipment(s)
    case CartAbandoned(_, sc) => abandonCart(sc)
  }
}

object Backorder {

  def empty = Backorder(None, Nil, Map.empty)
}
