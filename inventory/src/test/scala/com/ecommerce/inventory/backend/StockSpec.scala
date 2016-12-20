package com.ecommerce.inventory.backend

import java.net.URI
import java.util.UUID
import org.joda.time.DateTime

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/11/16.
  */
class StockSpec extends FlatSpec with Matchers {
  import Stock._

  "An InventoryItem" should "set the Product when new" in {
    val stock = Stock.empty

    val product = new ItemRef(UUID.randomUUID)
    val stockWithProduct = stock.setProduct(product)

    stockWithProduct.product match {
      case None => fail("InventoryItem doesn't specify a product")
      case Some(p) => p should be theSameInstanceAs product
    }
  }

  it should "not allow the product to be set if there already is a product" in {

  }

  it should "increase the in-stock count when a shipment is accepted" in {
    val item = Stock.empty.setProduct(ItemRef(UUID.randomUUID))

    val shipment = ShipmentRef(UUID.randomUUID, DateTime.now.plusDays(20), 10)
    val updatedItem = item.acceptShipment(shipment)

    updatedItem.count should be (10)
    updatedItem.availableCount should be (10)
  }

  it should "decrease the available count and maintain the in-stock count when placed in a shopping cart" in {
    val item = Stock.empty.setProduct(ItemRef(UUID.randomUUID))

    val shipment = ShipmentRef(UUID.randomUUID, DateTime.now.plusDays(20), 10)
    val updatedItem = item.acceptShipment(shipment)
    val shoppingCart = ShoppingCartRef(UUID.randomUUID)
    val itemWithHold = updatedItem.holdForCustomer(shoppingCart, 3)

    itemWithHold.count should be (10)
    itemWithHold.availableCount should be (7)
  }

  it should "count multiple shopping count holds and deduct the sum from the available count" in {
    val item = Stock.empty.setProduct(ItemRef(UUID.randomUUID))

    val shipment = ShipmentRef(UUID.randomUUID, DateTime.now.plusDays(20), 10)
    val updatedItem = item.acceptShipment(shipment)
    val shoppingCart1 = ShoppingCartRef(UUID.randomUUID)
    val itemWithHold = updatedItem.holdForCustomer(shoppingCart1, 3)
    val shoppingCart2 = ShoppingCartRef(UUID.randomUUID)
    val itemWith2Holds = itemWithHold.holdForCustomer(shoppingCart2, 1)

    itemWithHold.count should be (10)
    itemWith2Holds.availableCount should be (6)
  }

  it should "allow the on-hold"
}
