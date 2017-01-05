package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime

import Identity.{ItemRef, ReservationRef, ShipmentRef, ShoppingCartRef}

/**
  * Created by lukewyman on 12/18/16.
  */
case class Backorder(expectedShipments: List[ShipmentRef], reservations: Map[ReservationRef, Int]) {

  def acknowledgeShipment(shipment: ShipmentRef): Backorder = {
    copy(expectedShipments = shipment :: expectedShipments)
  }

  def acceptShipment(shipment: ShipmentRef): Backorder = ???

  def makeReservation(reservation: ReservationRef, count: Int): Backorder = {
    copy(reservations = reservations.updated(reservation, count))
  }

  def abandonCart(shoppingCart: ShoppingCartRef): Backorder = ???

  def count: Int = {
    expectedShipments.map(_.count).sum - reservations.values.sum
  }

  def availableCount(date: ZonedDateTime): Int = {
    val reservationCount = reservations.filter({ case (r, c) => r.shipmentRef.expectedDate == date }).values.sum
    expectedShipments.filter(_.expectedDate == date).map(_.count).sum - reservationCount
  }
}

object Backorder {

  def empty = Backorder(Nil, Map.empty)
}
