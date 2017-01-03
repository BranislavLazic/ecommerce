package com.ecommerce.inventory.backend

import java.lang.IllegalArgumentException
import java.net.URI
import java.time.ZonedDateTime
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/11/16.
  */
class StockSpec extends FlatSpec with Matchers {
  import Identity._

  "An InventoryItem" should "set the Product when new" in {
    val stock = Stock.empty

    val product = new ItemRef(UUID.randomUUID)
    val stockWithProduct = stock.setProduct(product)

    stockWithProduct.product match {
      case None => fail("Stock doesn't specify a product")
      case Some(p) => p should be theSameInstanceAs product
    }
  }

  it should "not allow the product to be set if there already is a product" in {
    val stockWithProduct = Stock.empty.setProduct(new ItemRef(UUID.randomUUID))
    val otherProduct = ItemRef(UUID.randomUUID)

    an [IllegalArgumentException] should be thrownBy stockWithProduct.setProduct(otherProduct)
  }

  it should "increase the in-stock count when a shipment is accepted" in {
    val item = Stock.empty.setProduct(ItemRef(UUID.randomUUID))

    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val updatedItem = item.acceptShipment(shipment)

    updatedItem.count should be (10)
    updatedItem.availableCount should be (10)
  }

  it should "decrease the available count and maintain the in-stock count when placed in a shopping cart" in {
    val item = Stock.empty.setProduct(ItemRef(UUID.randomUUID))

    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val updatedItem = item.acceptShipment(shipment)
    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val itemWithHold = updatedItem.holdForCustomer(shoppingCart, 3)

    itemWithHold.count should be (10)
    itemWithHold.availableCount should be (7)
  }

  it should "count multiple shopping cart holds and deduct their sum from the available count" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.setProduct(ItemRef(UUID.randomUUID)).acceptShipment(shipment)

    val shoppingCart1 = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart1, 3)
    val shoppingCart2 = ShoppingCartRef(UUID.randomUUID)
    val stockWith2Holds = stockWithHold.holdForCustomer(shoppingCart2, 1)

    stockWith2Holds.count should be (10)
    stockWith2Holds.availableCount should be (6)
  }

  it should "update the count on hold for a customer when their shopping cart is updated" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.setProduct(ItemRef(UUID.randomUUID)).acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart, 3)
    val stockWithUpdatedHold = stockWithHold.holdForCustomer(shoppingCart, 5)

    stockWithUpdatedHold.count should be (10)
    stockWithUpdatedHold.availableCount should be (5)
  }

  it should "increment the available count if a customer abandons their shopping cart" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.setProduct(ItemRef(UUID.randomUUID)).acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart, 3)
    val stockAfterAbandonment = stockWithHold.abandonCart(shoppingCart)

    stockAfterAbandonment.count should be (10)
    stockAfterAbandonment.availableCount should be (10)
  }

  it should "decrement the in-stock count if a customer checks out" in {
    val shipment = ShipmentRef(UUID.randomUUID, ZonedDateTime.now.plusDays(20), 10)
    val stock = Stock.empty.setProduct(ItemRef(UUID.randomUUID)).acceptShipment(shipment)

    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val stockWithHold = stock.holdForCustomer(shoppingCart, 3)
    val stockAfterCheckout = stockWithHold.checkout(shoppingCart)

    stockAfterCheckout.count should be (7)
    stockAfterCheckout.count should be (7)
  }
}
