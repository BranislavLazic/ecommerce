package com.ecommerce.inventory.backend

import akka.actor.Actor
import akka.cluster.sharding.ShardRegion

/**
  * Created by lukewyman on 12/18/16.
  */
object InventoryItem {

  def props = ???

  def name = ???

  val regionName = "inventoryitems"

  val extractEntityId: ShardRegion.ExtractEntityId = ???

  val extractShardId: ShardRegion.ExtractShardId = ???
}

class InventoryItem extends Actor {

  val stockManager = context.actorOf(StockManager.props, StockManager.name)

  val backorderManager = context.actorOf(BackorderManager.props, BackorderManager.name)

  def receive = ???

}
