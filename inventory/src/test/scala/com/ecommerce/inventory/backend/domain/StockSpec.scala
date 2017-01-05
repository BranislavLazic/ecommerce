package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/11/16.
  */
class StockSpec extends FlatSpec with Matchers {
  import Identity._

  it should "increase the in-stock count when a shipment is accepted" in {
    val stock = Stock.empty
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val updatedStock = stock.acceptShipment(shipment)

    updatedStock.count should be (10)
    updatedStock.availableCount should be (10)
  }

  it should "decrease the available count and maintain the in-stock count when placed in a shopping cart" in {
    val stock = Stock.empty

    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val updatedStock = stock.acceptShipment(shipment)
    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = updatedStock.holdForCustomer(shoppingCart, 3)

    stockWithHold.count should be (10)
    stockWithHold.availableCount should be (7)
  }

  it should "count multiple shopping cart holds and deduct their sum from the available count" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart1 = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart1, 3)
    val shoppingCart2 = ShoppingCartRef(UUID.randomUUID)
    val stockWith2Holds = stockWithHold.holdForCustomer(shoppingCart2, 1)

    stockWith2Holds.count should be (10)
    stockWith2Holds.availableCount should be (6)
  }

  it should "update the count on hold for a customer when their shopping cart is updated" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart, 3)
    val stockWithUpdatedHold = stockWithHold.holdForCustomer(shoppingCart, 5)

    stockWithUpdatedHold.count should be (10)
    stockWithUpdatedHold.availableCount should be (5)
  }

  it should "increment the available count if a customer abandons their shopping cart" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart, 3)
    val stockAfterAbandonment = stockWithHold.abandonCart(shoppingCart)

    stockAfterAbandonment.count should be (10)
    stockAfterAbandonment.availableCount should be (10)
  }

  it should "decrement the in-stock count if a customer checks out" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart, 3)
    val stockAfterCheckout = stockWithHold.checkout(shoppingCart)

    stockAfterCheckout.count should be (7)
    stockAfterCheckout.count should be (7)
  }
}
