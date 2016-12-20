package com.ecommerce.inventory.api

import akka.actor.{ActorSystem, ActorRef}
import akka.util.Timeout

/**
  * Created by lukewyman on 12/18/16.
  */
class InventoryService(val inventoryItems: ActorRef, val system: ActorSystem, val requestTimeout: Timeout) extends InventoryRoutes {
  val executionContext = system.dispatcher
}

trait InventoryRoutes {

}
