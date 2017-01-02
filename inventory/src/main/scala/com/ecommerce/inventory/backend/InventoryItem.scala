package com.ecommerce.inventory.backend

import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import akka.pattern.ask
import akka.cluster.sharding.ShardRegion
import akka.util.Timeout
import com.ecommerce.inventory.backend.Identity._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by lukewyman on 12/18/16.
  */
object InventoryItem {

  def props = Props(new InventoryItem)

  def name = "inventory-item"

  val regionName = "inventoryitems"

  val extractEntityId: ShardRegion.ExtractEntityId = ???

  val extractShardId: ShardRegion.ExtractShardId = ???

  sealed trait Command
  case class SetProduct(inventoryItem: ItemRef)
  case class HoldItems(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef, stockCount: Int, backorderCount: Int)
  case class Checkout(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef, payment: PaymentRef)
  case class ReleaseItems(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef)
  case class AcceptShipment(inventoryItem: ItemRef, shipment: ShipmentRef)
  case class AcknowledgeShipment(inventoryItem: ItemRef, shipment: ShipmentRef)

  case class ProductChanged(inventoryItem: ItemRef)
  case class ItemsHeld(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef, stockCount: Int, backorderCount: Int)
  case class CheckedOut(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef)

  case class GetItem(inventoryItem: ItemRef)
  case class GetItemResult(inventoryItem: ItemRef)
}

class InventoryItem extends Actor with StockApi with BackorderApi with InventoryItemSupport {
  import InventoryItem._

  implicit def executionContext: ExecutionContext = context.system.dispatcher
  implicit def timeout: Timeout = Timeout(3 seconds)

  val stockManager = context.actorOf(StockManager.props, StockManager.name)

  val backorderManager = context.actorOf(BackorderManager.props, BackorderManager.name)

  def receive = {
    case SetProduct(item) =>
      val sf = getStockEvent(StockMessage.SetProduct(item))
      val bof = getBackorderEvent(BackorderMessage.SetProduct(item))

    case HoldItems(item, cart, scount, bocount) =>

    case Checkout(item, cart, payment) =>
      val sf = getStockEvent(StockMessage.Checkout(item, cart))

    case ReleaseItems(item, cart) =>

    case AcceptShipment(item, ship) =>

    case AcknowledgeShipment(item, ship) =>
  }

}

trait StockApi { this: Actor =>
  import StockMessage._
  import StockManager._

  def stockManager: ActorRef

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def getStockEvent(cmd: Command): Future[Either[Event, String]] =
    stockManager.ask(cmd).map {
      case event: Event => Left(event)
      case Rejection(reason) => Right(reason)
    }
}

trait BackorderApi { this: Actor =>
  import BackorderMessage._
  import BackorderManager._

  def backorderManager: ActorRef

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def getBackorderEvent(cmd: Command): Future[Either[Event, String]] =

    backorderManager.ask(cmd).map {
      case event: Event => Left(event)
      case Rejection(reason) => Right(reason)
    }
}

trait InventoryItemSupport {

  import com.ecommerce.inventory.backend.{StockMessage => SM}
  import com.ecommerce.inventory.backend.{BackorderMessage => BOM}

  implicit def executionContext: ExecutionContext


}




