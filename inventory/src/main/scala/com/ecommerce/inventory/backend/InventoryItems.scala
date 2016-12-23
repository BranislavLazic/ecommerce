package com.ecommerce.inventory.backend

import akka.actor.{Props, Actor}
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding}

/**
  * Created by lukewyman on 12/18/16.
  */
object InventoryItems {

  def props = Props(new InventoryItems())

  def name = "inventoryitems"
}

class InventoryItems extends Actor {

  ClusterSharding(context.system).start(
    InventoryItem.regionName,
    InventoryItem.props,
    ClusterShardingSettings(context.system),
    InventoryItem.extractEntityId,
    InventoryItem.extractShardId
  )

  def inventoryItem = ClusterSharding(context.system).shardRegion(InventoryItem.regionName)

  def receive = ???
}
