package com.ecommerce.shoppingcart.backend

import java.util.UUID

import akka.actor.ActorSystem
import com.ecommerce.common.identity.Identity._

/**
  * Created by lukewyman on 1/24/17.
  */
class ShoppingCartManagerSpec extends PersistenceSpec(ActorSystem("test")) with PersistenceCleanup {

  "A ShoppingCartManager" should {
    "place items in the ShoppingCart and then view the ShoppingCart" in {
      val shoppingCartId = ShoppingCartRef(UUID.randomUUID)
      val customerId = CustomerRef(UUID.randomUUID)
      val item1 = ProductRef(UUID.randomUUID())
      val item2 = ProductRef(UUID.randomUUID())
      val shoppingCartManager = system.actorOf(ShoppingCartManager.props, ShoppingCartManager.name(shoppingCartId))

      shoppingCartManager ! SetOwner(shoppingCartId, customerId)
      expectMsg(GetShoppingCartResult(shoppingCartId, ShoppingCart(Map.empty, Some(customerId))))
      shoppingCartManager ! AddItem(shoppingCartId, item1, 1)
      expectMsg(GetShoppingCartResult(shoppingCartId, ShoppingCart(Map((item1 -> 1)), Some(customerId))))
      shoppingCartManager ! AddItem(shoppingCartId, item2, 3)
      expectMsg(GetShoppingCartResult(shoppingCartId, ShoppingCart(Map((item1 -> 1), (item2 -> 3)), Some(customerId))))
      shoppingCartManager ! GetItems(shoppingCartId)
      expectMsg(GetShoppingCartResult(shoppingCartId, ShoppingCart(Map((item1 -> 1), (item2 -> 3)), Some(customerId))))
      killActors(shoppingCartManager)
    }

    "replay events on recovery and reach the correct state" in {

    }
  }
}
