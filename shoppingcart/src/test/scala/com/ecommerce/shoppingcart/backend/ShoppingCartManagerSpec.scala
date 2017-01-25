package com.ecommerce.shoppingcart.backend

import java.util.UUID

import akka.actor.ActorSystem
import com.ecommerce.shoppingcart.backend.ShoppingCart.{ItemRef, CustomerRef, ShoppingCartRef}

/**
  * Created by lukewyman on 1/24/17.
  */
class ShoppingCartManagerSpec extends PersistenceSpec(ActorSystem("test")) with PersistenceCleanup {

  "A ShoppingCartManager" should {
    "place items in the ShoppingCart and then view the ShoppingCart" in {
      val shoppingCartId = ShoppingCartRef(UUID.randomUUID)
      val customerId = CustomerRef(UUID.randomUUID)
      val item1 = ItemRef(UUID.randomUUID())
      val item2 = ItemRef(UUID.randomUUID())
      val shoppingCartManager = system.actorOf(ShoppingCartManager.props, ShoppingCartManager.name(shoppingCartId))

      shoppingCartManager ! SetOwner(shoppingCartId, customerId)
      expectMsg(OwnerChanged(shoppingCartId, customerId))
      shoppingCartManager ! AddItem(shoppingCartId, item1, 1)
      expectMsg(ItemAdded(shoppingCartId, item1, 1))
      shoppingCartManager ! AddItem(shoppingCartId, item2, 3)
      expectMsg(ItemAdded(shoppingCartId, item2, 3))
      shoppingCartManager ! GetItems(shoppingCartId)
      expectMsg(GetItemsResult(shoppingCartId, Map((item1 -> 1), (item2 -> 3))))
      killActors(shoppingCartManager)
    }
    "replay events on recovery and reach the correct state" in {
      
    }
  }
}
