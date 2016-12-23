package com.ecommerce.inventory.backend

import java.util.UUID

import com.ecommerce.inventory.backend.Backorder.{CustomerRef, Reservation, ShipmentRef, ItemRef}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/18/16.
  */
class BackorderSpec extends FlatSpec with Matchers {

  "A Backorder" should "set the product when new" in {
    val item = new ItemRef(UUID.randomUUID)
    val backOrder = Backorder.empty.setProduct(item)

    backOrder.product match {
      case None => fail("Backorder doesn't specify a product")
      case Some(p) => p should be theSameInstanceAs item
    }
  }

  it should "not allow product to be set if there already is a product" in {
    val item = ItemRef(UUID.randomUUID)
    val backOrderWithProduct = Backorder.empty.setProduct(item)

    val otherItem = ItemRef(UUID.randomUUID)
    an [IllegalArgumentException] should be thrownBy backOrderWithProduct.setProduct(otherItem)
  }

  it should "increment the backorder count when a shipment is acknowledged" in {
    val backOrder = Backorder.empty.setProduct(new ItemRef(UUID.randomUUID))

    val expectedDate = DateTime.now.plusDays(20)
    val updatedBackorder = backOrder.acknowledgeShipment(ShipmentRef(UUID.randomUUID, expectedDate, 10))

    updatedBackorder.availableCount(expectedDate) should be (10)
  }

  it should "calculate the available count for expected shipments for the given date" in {
    val backorder = Backorder.empty.setProduct(new ItemRef(UUID.randomUUID))

    val expectedDate = DateTime.now.plusDays(20)
    val otherDate = DateTime.now.plusDays(25)
    val updatedBackorder = backorder.acknowledgeShipment(ShipmentRef(UUID.randomUUID, expectedDate, 10))
      .acknowledgeShipment(ShipmentRef(UUID.randomUUID, otherDate, 5))

    updatedBackorder.availableCount(expectedDate) should be (10)
  }

  it should "calculate the total count for all the expected shipments" in {
    val backorder = Backorder.empty.setProduct(new ItemRef(UUID.randomUUID))

    val date1 = DateTime.now.plusDays(20)
    val date2 = DateTime.now.plusDays(25)
    val updatedBackorder = backorder.acknowledgeShipment(ShipmentRef(UUID.randomUUID, date1, 10))
      .acknowledgeShipment(ShipmentRef(UUID.randomUUID, date2, 5))

    updatedBackorder.count should be (15)
  }

  it should "deduct reservations for a given date from the available count" in {
    val backorder = Backorder.empty.setProduct(new ItemRef(UUID.randomUUID))

    val expectedDate = DateTime.now.plusDays(20)
    val shipment = ShipmentRef(UUID.randomUUID, expectedDate, 10)
    val updatedBackorder = backorder.acknowledgeShipment(shipment)

    val reservation = Reservation(CustomerRef(UUID.randomUUID), shipment)
    val backorderWithReservation = updatedBackorder.makeReservation(reservation, 3)

    backorderWithReservation.availableCount(expectedDate) should be (7)
  }

  it should "deduct the sum of all reservations when calculating the total count" in {
    val backorder = Backorder.empty.setProduct(new ItemRef(UUID.randomUUID))

    val date1 = DateTime.now.plusDays(20)
    val date2 = DateTime.now.plusDays(25)
    val shipment1 = ShipmentRef(UUID.randomUUID, date1, 10)
    val shipment2 = ShipmentRef(UUID.randomUUID, date2, 5)
    val reservation1 = Reservation(CustomerRef(UUID.randomUUID), shipment1)
    val reservation2 = Reservation(CustomerRef(UUID.randomUUID), shipment2)
    val updatedBackorder = backorder.acknowledgeShipment(shipment1)
      .acknowledgeShipment(shipment2)
      .makeReservation(reservation1, 3)
      .makeReservation(reservation2, 2)

    updatedBackorder.count should be (10)
  }
}
