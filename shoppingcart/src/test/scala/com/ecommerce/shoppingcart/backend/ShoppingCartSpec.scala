package com.ecommerce.shoppingcart.backend

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by lukewyman on 12/11/16.
  */
class ShoppingCartSpec extends FlatSpec with Matchers {
  import ShoppingCart._

  "A ShoppingCart" should "set the owner when new" in {
    val cart = ShoppingCart.empty

    val customer = new CustomerRef(UUID.randomUUID)
    val ownedCart = cart.setOwner(customer)

    ownedCart.owner.map(customer => customer should be theSameInstanceAs customer)
  }

  it should "not allow the owner to be set if there already is an owner" in {
    val cart = ShoppingCart.empty

    val customer = new CustomerRef(UUID.randomUUID)
    val ownedCart = cart.setOwner(customer)

    val customer2 = new CustomerRef(UUID.randomUUID)
    an [IllegalArgumentException] should be thrownBy ownedCart.setOwner(customer2)
  }

  it should "add a new item to the cart" in {
    val cart = ShoppingCart.empty
    val item = ItemRef(UUID.randomUUID)

    val updatedCart = cart.addItem(item, 1)

    updatedCart.items should contain (item -> 1)
  }

  it should "increase the count of items in a cart" in {
    val item = ItemRef(UUID.randomUUID)
    val cart = ShoppingCart.empty.addItem(item, 1)

    val updatedCart = cart.addItem(item, 2)

    updatedCart.items should contain (item -> 3)
  }

  it should "remove an item from a cart" in {
    val item = ItemRef(UUID.randomUUID)
    val cart = ShoppingCart.empty.addItem(item, 1)

    val updatedCart = cart.removeItem(item, 1)

    updatedCart.items shouldBe empty
  }

  it should "decrease the count of items in a cart" in {
    val item = ItemRef(UUID.randomUUID)
    val cart = ShoppingCart.empty.addItem(item, 5)

    val updatedCart = cart.removeItem(item, 2)

    updatedCart.items should contain (item -> 3)
  }

  it should "not allow a negative count of items to be added" in {
    val item = ItemRef(UUID.randomUUID)
    val cart = ShoppingCart.empty

    an [IllegalArgumentException] should be thrownBy cart.addItem(item, - 1)
  }

  it should "not allow a zero count of items to be added" in {
    val item = ItemRef(UUID.randomUUID)
    val cart = ShoppingCart.empty

    an [IllegalArgumentException] should be thrownBy cart.addItem(item, 0)
  }

  it should "not allow a negative count of items to be removed" in {
    val item = ItemRef(UUID.randomUUID)
    val cart = ShoppingCart.empty

    an [IllegalArgumentException] should be thrownBy cart.removeItem(item, -1)
  }

  it should "not allow a zero count of items to be removed" in {
    val item = ItemRef(UUID.randomUUID)
    val cart = ShoppingCart.empty

    an [IllegalArgumentException] should be thrownBy cart.removeItem(item, 0)
  }
}