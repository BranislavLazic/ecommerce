package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime
import java.util.UUID

import com.ecommerce.common.identity.Identity.{CustomerRef, ShipmentRef}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/18/16.
  */
class BackorderSpec extends FlatSpec with Matchers {

//  "A Backorder" should "increment the backorder available count when a shipment is acknowledged" in {
//    val backOrder = Backorder.empty
//
//    val expectedDate = ZonedDateTime.now.plusDays(20)
//    val updatedBackorder = backOrder.acknowledgeShipment(Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(10), null, 10))
//
//    updatedBackorder.availableCount(expectedDate) should be (10)
//  }

  it should "decrement the backorder available count when a shipment is accepted" in {
    val backOrder = Backorder.empty

    val expectedDate = ZonedDateTime.now.plusDays(20)
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), expectedDate, null, 10)
    val ackBackorder = backOrder.acknowledgeShipment(shipment)
    val acceptBackorder = ackBackorder.acceptShipment(shipment)

    acceptBackorder.availableCount(expectedDate) should be (0)
  }

  it should "calculate the available count for expected shipments for the given date" in {
    val backorder = Backorder.empty

    val expectedDate = ZonedDateTime.now.plusDays(20)
    val otherDate = ZonedDateTime.now.plusDays(25)
    val shipment1 = Shipment(ShipmentRef(UUID.randomUUID), expectedDate, null, 10)
    val shipment2 = Shipment(ShipmentRef(UUID.randomUUID), otherDate, null, 10)
    val updatedBackorder = backorder.acknowledgeShipment(shipment1)
      .acknowledgeShipment(shipment2)

    updatedBackorder.availableCount(expectedDate) should be (10)
  }

  it should "calculate the total count for all the expected shipments" in {
    val backorder = Backorder.empty

    val date1 = ZonedDateTime.now.plusDays(20)
    val date2 = ZonedDateTime.now.plusDays(25)
    val shipment1 = Shipment(ShipmentRef(UUID.randomUUID), date1, null, 10)
    val shipment2 = Shipment(ShipmentRef(UUID.randomUUID), date2, null, 5)
    val updatedBackorder = backorder.acknowledgeShipment(shipment1)
      .acknowledgeShipment(shipment2)

    updatedBackorder.totalCount should be (15)
  }

  it should "deduct reservations for a given date from the available count" in {
    val backorder = Backorder.empty

    val expectedDate = ZonedDateTime.now.plusDays(20)
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), expectedDate, null, 10)
    val updatedBackorder = backorder.acknowledgeShipment(shipment)

    val reservation = Reservation(CustomerRef(UUID.randomUUID), shipment)
    val backorderWithReservation = updatedBackorder.makeReservation(reservation, 3)

    backorderWithReservation.availableCount(expectedDate) should be (7)
  }

  it should "deduct the sum of all reservations when calculating the total count" in {
    val backorder = Backorder.empty

    val date1 = ZonedDateTime.now.plusDays(20)
    val date2 = ZonedDateTime.now.plusDays(25)
    val shipment1 = Shipment(ShipmentRef(UUID.randomUUID), date1, null, 10)
    val shipment2 = Shipment(ShipmentRef(UUID.randomUUID), date2, null, 5)
    val reservation1 = Reservation(CustomerRef(UUID.randomUUID), shipment1)
    val reservation2 = Reservation(CustomerRef(UUID.randomUUID), shipment2)
    val updatedBackorder = backorder.acknowledgeShipment(shipment1)
      .acknowledgeShipment(shipment2)
      .makeReservation(reservation1, 3)
      .makeReservation(reservation2, 2)

    updatedBackorder.totalCount should be (10)
  }

  it should "do calculate backorder count after placing and releasing holds" in {
    val backorder = Backorder.empty

    val date1 = ZonedDateTime.now.plusDays(20)
    val date2 = ZonedDateTime.now.plusDays(25)
    val shipment1 = Shipment(ShipmentRef(UUID.randomUUID), date1, null, 10)
    val shipment2 = Shipment(ShipmentRef(UUID.randomUUID), date2, null, 5)
    val customer1 = CustomerRef(UUID.randomUUID)
    val customer2 = CustomerRef(UUID.randomUUID)
    val reservation1 = Reservation(customer1, shipment1)
    val reservation2 = Reservation(customer2, shipment2)
    val updatedBackorder = backorder.acknowledgeShipment(shipment1)
      .acknowledgeShipment(shipment2)
      .makeReservation(reservation1, 3)
      .makeReservation(reservation2, 2)
      .releaseReservation(customer1)

    updatedBackorder.totalCount should be (13)
  }

}
