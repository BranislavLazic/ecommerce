package com.ecommerce.inventory.backend

import Stock._
import org.joda.time.DateTime

/**
  * Created by lukewyman on 12/18/16.
  */
case class Backorder(product: Option[ItemRef], count: Int, shipments: List[ShipmentRef], reservations: List[Reservation]) {

  def setProduct(product: ItemRef): Backorder = ???

  def placeReservation(customer: CustomerRef, count: Int): Backorder = ???

  def acknowledgeShipment(shipment: ShipmentRef): Backorder = ???

  def acceptShipment(shipment: ShipmentRef): Backorder = ???

  def availableCount(date: DateTime): Int = ???
}
