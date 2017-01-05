package com.ecommerce.inventory.backend

import akka.actor.{Props, Actor}
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding}
import com.ecommerce.inventory.backend.InventoryItemManager.{Query, Command}

/**
  * Created by lukewyman on 12/18/16.
  */
object InventoryItems {

  def props = Props(new InventoryItems)

  def name = "inventory-items"
}

class InventoryItems extends Actor {

  ClusterSharding(context.system).start(
    ShardSupport.regionName,
    InventoryItemManager.props,
    ClusterShardingSettings(context.system),
    ShardSupport.extractEntityId,
    ShardSupport.extractShardId
  )

  def inventoryItem = ClusterSharding(context.system).shardRegion(ShardSupport.regionName)

  def receive = {
    case cmd: Command => inventoryItem forward cmd
    case qry: Query => inventoryItem forward qry
  }
}
