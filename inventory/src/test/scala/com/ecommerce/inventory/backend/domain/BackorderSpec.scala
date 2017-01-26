package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/18/16.
  */
class BackorderSpec extends FlatSpec with Matchers {
  import Identity._

  "A Backorder" should "increment the backorder available count when a shipment is acknowledged" in {
    val backOrder = Backorder.empty

    val expectedDate = ZonedDateTime.now.plusDays(20)
    val updatedBackorder = backOrder.acknowledgeShipment(ShipmentRef(UUID.randomUUID, expectedDate, 10))

    updatedBackorder.availableCount(expectedDate) should be (10)
  }

  it should "decrement the backorder available count when a shipment is accepted" in {
    val backOrder = Backorder.empty

    val expectedDate = ZonedDateTime.now.plusDays(20)
    val shipment = ShipmentRef(UUID.randomUUID, expectedDate, 10)
    val ackBackorder = backOrder.acknowledgeShipment(shipment)
    val acceptBackorder = ackBackorder.acceptShipment(shipment)

    acceptBackorder.availableCount(expectedDate) should be (0)
  }

  it should "calculate the available count for expected shipments for the given date" in {
    val backorder = Backorder.empty

    val expectedDate = ZonedDateTime.now.plusDays(20)
    val otherDate = ZonedDateTime.now.plusDays(25)
    val updatedBackorder = backorder.acknowledgeShipment(ShipmentRef(UUID.randomUUID, expectedDate, 10))
      .acknowledgeShipment(ShipmentRef(UUID.randomUUID, otherDate, 5))

    updatedBackorder.availableCount(expectedDate) should be (10)
  }

  it should "calculate the total count for all the expected shipments" in {
    val backorder = Backorder.empty

    val date1 = ZonedDateTime.now.plusDays(20)
    val date2 = ZonedDateTime.now.plusDays(25)
    val updatedBackorder = backorder.acknowledgeShipment(ShipmentRef(UUID.randomUUID, date1, 10))
      .acknowledgeShipment(ShipmentRef(UUID.randomUUID, date2, 5))

    updatedBackorder.totalCount should be (15)
  }

  it should "deduct reservations for a given date from the available count" in {
    val backorder = Backorder.empty

    val expectedDate = ZonedDateTime.now.plusDays(20)
    val shipment = ShipmentRef(UUID.randomUUID, expectedDate, 10)
    val updatedBackorder = backorder.acknowledgeShipment(shipment)

    val reservation = ReservationRef(CustomerRef(UUID.randomUUID), shipment)
    val backorderWithReservation = updatedBackorder.makeReservation(reservation, 3)

    backorderWithReservation.availableCount(expectedDate) should be (7)
  }

  it should "deduct the sum of all reservations when calculating the total count" in {
    val backorder = Backorder.empty

    val date1 = ZonedDateTime.now.plusDays(20)
    val date2 = ZonedDateTime.now.plusDays(25)
    val shipment1 = ShipmentRef(UUID.randomUUID, date1, 10)
    val shipment2 = ShipmentRef(UUID.randomUUID, date2, 5)
    val reservation1 = ReservationRef(CustomerRef(UUID.randomUUID), shipment1)
    val reservation2 = ReservationRef(CustomerRef(UUID.randomUUID), shipment2)
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
    val shipment1 = ShipmentRef(UUID.randomUUID, date1, 10)
    val shipment2 = ShipmentRef(UUID.randomUUID, date2, 5)
    val customer1 = CustomerRef(UUID.randomUUID)
    val customer2 = CustomerRef(UUID.randomUUID)
    val reservation1 = ReservationRef(customer1, shipment1)
    val reservation2 = ReservationRef(customer2, shipment2)
    val updatedBackorder = backorder.acknowledgeShipment(shipment1)
      .acknowledgeShipment(shipment2)
      .makeReservation(reservation1, 3)
      .makeReservation(reservation2, 2)
      .releaseReservation(customer1)

    updatedBackorder.totalCount should be (13)
  }

}
