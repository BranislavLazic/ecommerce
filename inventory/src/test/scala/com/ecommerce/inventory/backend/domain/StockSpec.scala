package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime
import java.util.UUID
import com.ecommerce.common.identity.Identity._
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/11/16.
  */
class StockSpec extends FlatSpec with Matchers {

  "A Stock" should "increase the in-stock count when a shipment is accepted" in {
    val stock = Stock.empty
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
    val updatedStock = stock.acceptShipment(shipment)

    updatedStock.inStock should be (10)
    updatedStock.availableCount should be (10)
  }

  it should "decrease the available count and maintain the in-stock count when placed in a shopping cart" in {
    val stock = Stock.empty

    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
    val updatedStock = stock.acceptShipment(shipment)
    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = updatedStock.placeHold(shoppingCart, 3)

    stockWithHold.inStock should be (10)
    stockWithHold.availableCount should be (7)
  }

  it should "count multiple shopping cart holds and deduct their sum from the available count" in {
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart1 = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.placeHold(shoppingCart1, 3)
    val shoppingCart2 = ShoppingCartRef(UUID.randomUUID)
    val stockWith2Holds = stockWithHold.placeHold(shoppingCart2, 1)

    stockWith2Holds.inStock should be (10)
    stockWith2Holds.availableCount should be (6)
  }

  it should "update the count on hold for a customer when their shopping cart is updated" in {
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.placeHold(shoppingCart, 3)
    val stockWithUpdatedHold = stockWithHold.placeHold(shoppingCart, 5)

    stockWithUpdatedHold.inStock should be (10)
    stockWithUpdatedHold.availableCount should be (5)
  }

  it should "increment the available count if a customer abandons their shopping cart" in {
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.placeHold(shoppingCart, 3)
    val stockAfterAbandonment = stockWithHold.releaseHold(shoppingCart)

    stockAfterAbandonment.inStock should be (10)
    stockAfterAbandonment.availableCount should be (10)
  }

  it should "decrement the in-stock count if a customer checks out" in {
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.placeHold(shoppingCart, 3)
    val stockAfterCheckout = stockWithHold.claimItem(shoppingCart)

    stockAfterCheckout.inStock should be (7)
    stockAfterCheckout.inStock should be (7)
  }

//  it should "not allow a Hold to placed that is greater than the in stock amount" in {
//    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
//    val stock = Stock.empty.acceptShipment(shipment)
//
//    val cart = ShoppingCartRef(UUID.randomUUID())
//    an [IllegalArgumentException] should be thrownBy(stock.placeHold(cart, 5))
//  }

  it should "not allow a Hold to be claimed that does not exist" in {
    val shipment = Shipment(ShipmentRef(UUID.randomUUID), ZonedDateTime.now.plusDays(-10), ZonedDateTime.now, 10)
    val stock = Stock.empty.acceptShipment(shipment)

    val cart = ShoppingCartRef(UUID.randomUUID())
    an [IllegalArgumentException] should be thrownBy(stock.claimItem(cart))
  }
}
