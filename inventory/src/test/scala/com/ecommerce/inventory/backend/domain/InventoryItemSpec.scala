package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime
import java.util.UUID

import com.ecommerce.inventory.backend.domain.Identity.ItemRef
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 1/4/17.
  */
class InventoryItemSpec extends FlatSpec with Matchers {

  "An InventoryItem" should "set the product when new" in {
    val product = ItemRef(UUID.randomUUID)
    val item = InventoryItem.empty.setProduct(product)

    item.product match {
      case None => fail("InventoryItem doesn't specify a product")
      case Some(p) => p should be theSameInstanceAs product
    }
  }

  it should "not allow the product to be set if there already is a product" in {
    val product = ItemRef(UUID.randomUUID)
    val item = InventoryItem.empty.setProduct(product)

    val otherProduct = ItemRef(UUID.randomUUID)
    an [IllegalArgumentException] should be thrownBy item.setProduct(otherProduct)
  }

}
