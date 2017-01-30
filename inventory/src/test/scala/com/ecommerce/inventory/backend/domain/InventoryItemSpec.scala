package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime
import java.util.UUID

import com.ecommerce.inventory.backend.InventoryItemManager.{ShipmentAccepted, CheckedOut, ItemHeld, ProductChanged}
import com.ecommerce.inventory.backend.domain.Identity.{ShipmentRef, ShoppingCartRef, ItemRef}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 1/4/17.
  */
class InventoryItemSpec extends FlatSpec with Matchers {

  "An InventoryItem" should "set the product when new" in {
    val product = ItemRef(UUID.randomUUID)
    val item = InventoryItem.empty.setProduct(product)

    item.product.fold(fail("InventoryItem doesn't specify a product"))(_ should be theSameInstanceAs product)
  }

  it should "not allow the product to be set if there already is a product" in {
    val product = ItemRef(UUID.randomUUID)
    val item = InventoryItem.empty.setProduct(product)

    val otherProduct = ItemRef(UUID.randomUUID)
    an [IllegalArgumentException] should be thrownBy item.setProduct(otherProduct)
  }

  it should "apply the ProductChanged event" in {
    val product = ItemRef(UUID.randomUUID())
    val item = InventoryItem.empty.applyEvent(ProductChanged(product))

    item.product.fold(fail("InventoryItem doesn't specify a product"))(_ should be theSameInstanceAs product)
  }

  it should "apply events for shipment accepted" in {

  }

  it should "apply events for shipment acknowledged" in {

  }

  it should "apply events for holding an item" in {

  }

  it should "apply events for making a reservation" in {

  }

  it should "apply events for checking out" in {
    val product = ItemRef(UUID.randomUUID)
    val item = InventoryItem.empty.setProduct(product)
  }

  it should "apply events for cart abandoned" in {

  }


}
