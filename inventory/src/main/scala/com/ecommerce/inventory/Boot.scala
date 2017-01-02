package com.ecommerce.inventory

import akka.actor.ActorSystem
import com.ecommerce.inventory.api.InventoryServiceSupport
import com.ecommerce.inventory.backend.InventoryItems

/**
  * Created by lukewyman on 12/18/16.
  */
object Boot extends App with InventoryServiceSupport {
  implicit val system = ActorSystem("inventory")

  val inventoryItems = system.actorOf(InventoryItems.props)

  start(inventoryItems)
}
