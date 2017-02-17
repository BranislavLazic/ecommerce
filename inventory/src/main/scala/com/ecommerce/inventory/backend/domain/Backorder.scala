package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime
import com.ecommerce.common.identity.Identity._

/**
  * Created by lukewyman on 12/18/16.
  */
case class Backorder(expectedShipments: Seq[Shipment], reservations: Map[Reservation, Int]) {

  def acknowledgeShipment(shipment: Shipment): Backorder = {
    copy(expectedShipments = expectedShipments :+ shipment)
  }

  def acceptShipment(shipment: Shipment): Backorder = {
    copy(expectedShipments = expectedShipments.filterNot(_.equals(shipment)))
  }

  def makeReservation(reservation: Reservation, count: Int): Backorder = {
    copy(reservations = reservations.updated(reservation, count))
  }

  def releaseReservation(customer: CustomerRef): Backorder = {
    copy(reservations = reservations.filterNot(_._1.customerId.equals(customer)))
  }

  def totalCount: Int = {
    expectedShipments.map(_.count).sum - reservations.values.sum
  }

  def availableCount(date: ZonedDateTime): Int = {
    val reservationTotal = reservations.filter(_._1.shipment.expectedDelivery == date).values.sum
    val expectedTotal = expectedShipments.filter(_.expectedDelivery == date).map(_.count).sum
    expectedTotal - reservationTotal
  }
}

object Backorder {

  def empty = Backorder(Nil, Map.empty)
}
