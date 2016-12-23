package com.ecommerce.inventory.backend

import java.util.UUID

import akka.actor.Actor
import akka.pattern.ask
import akka.cluster.sharding.ShardRegion
import com.ecommerce.inventory.backend.Identity._
import org.joda.time.DateTime

/**
  * Created by lukewyman on 12/18/16.
  */
object InventoryItem {

  def props = ???

  def name = ???

  val regionName = "inventoryitems"

  val extractEntityId: ShardRegion.ExtractEntityId = ???

  val extractShardId: ShardRegion.ExtractShardId = ???

  case class SetProduct(inventoryItem: ItemRef)
  case class HoldItems(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef)
  case class ClaimItems(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef)
  case class ReleaseItems(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef)
  case class AcceptShipment(inventoryItem: ItemRef, shipment: ShipmentRef)
  case class AcknowledgeShipment(inventoryItem: ItemRef, shipment: ShipmentRef)
}

class InventoryItem extends Actor {
  import InventoryItem._

  val stockManager = context.actorOf(StockManager.props, StockManager.name)

  val backorderManager = context.actorOf(BackorderManager.props, BackorderManager.name)

  def receive = {
    case SetProduct(item) =>
      stockManager ! StockManager.ManagerCommand(StockMessage.SetProduct(item), 1, self)
      backorderManager ! BackorderManager.ManagerCommand(BackorderMessage.SetProduct(item), 1, self)
    case HoldItems(item, cart) =>
    case ClaimItems(item, cart) =>
    case ReleaseItems(item, cart) =>
    case AcceptShipment(item, ship) =>
    case AcknowledgeShipment(item, ship) =>
  }

}
