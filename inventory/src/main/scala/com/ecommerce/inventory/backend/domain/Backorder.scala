package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime

import com.ecommerce.inventory.backend.domain.Identity._

/**
  * Created by lukewyman on 12/18/16.
  */
case class Backorder(expectedShipments: Seq[ShipmentRef], reservations: Map[ReservationRef, Int]) {

  def acknowledgeShipment(shipment: ShipmentRef): Backorder = {
    copy(expectedShipments = expectedShipments :+ shipment)
  }

  def acceptShipment(shipment: ShipmentRef): Backorder = {
    copy(expectedShipments = expectedShipments.filterNot(_.equals(shipment)))
  }

  def makeReservation(reservation: ReservationRef, count: Int): Backorder = {
    copy(reservations = reservations.updated(reservation, count))
  }

  def releaseReservation(customer: CustomerRef): Backorder = {
    copy(reservations = reservations.filterNot(_._1.customer.equals(customer)))
  }

  def totalCount: Int = {
    expectedShipments.map(_.count).sum - reservations.values.sum
  }

  def availableCount(date: ZonedDateTime): Int = {
    val reservationTotal = reservations.filter({ case (r, c) => r.shipmentRef.expectedDate == date }).values.sum
    expectedShipments.filter(_.expectedDate == date).map(_.count).sum - reservationTotal
  }
}

object Backorder {

  def empty = Backorder(Nil, Map.empty)
}
