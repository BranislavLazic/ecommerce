package com.ecommerce.inventory.backend

import akka.actor.Props
import akka.persistence.PersistentActor
import com.ecommerce.inventory.backend.domain.{InventoryItem, Identity}

/**
  * Created by lukewyman on 12/18/16.
  */
object InventoryItemManager {
  import Identity._

  def props = Props(new InventoryItemManager)

  def namei(ir: ItemRef) = ir.id.toString

  trait InventoryMessage {
    def item: ItemRef
  }

  sealed trait Command extends InventoryMessage
  case class SetProduct(item: ItemRef) extends Command
  case class HoldItem(item: ItemRef, shoppingCart: ShoppingCartRef, count: Int) extends Command
  case class MakeReservation(item: ItemRef, reservation: ReservationRef, count: Int) extends Command
  case class Checkout(item: ItemRef, shoppingCart: ShoppingCartRef, payment: PaymentRef) extends Command
  case class AbandonCart(item: ItemRef, shoppingCart: ShoppingCartRef) extends Command
  case class AcceptShipment(item: ItemRef, shipment: ShipmentRef) extends Command
  case class AcknowledgeShipment(item: ItemRef, shipment: ShipmentRef) extends Command

  sealed trait Event extends InventoryMessage with Serializable
  case class ProductChanged(item: ItemRef) extends Event
  case class ItemHeld(item: ItemRef, shoppingCart: ShoppingCartRef, count: Int) extends Event
  case class ReservationMade(item: ItemRef, reservation: ReservationRef, count: Int) extends Event
  case class CheckedOut(item: ItemRef, shoppingCart: ShoppingCartRef, payment: PaymentRef) extends Event
  case class CartAbandoned(item: ItemRef, shoppingCart: ShoppingCartRef, customer: CustomerRef) extends Event
  case class ShipmentAccepted(item: ItemRef, shipment: ShipmentRef) extends Event
  case class ShipmentAcknowledged(item: ItemRef, shipment: ShipmentRef) extends Event

  case class Rejection(reason: String)

  sealed trait Query extends InventoryMessage
  case class GetItem(item: ItemRef) extends Query

  sealed trait Result extends InventoryMessage
  case class GetItemResult(item: ItemRef) extends Result
}

class InventoryItemManager extends PersistentActor {
  import InventoryItemManager._

  override def persistenceId = context.parent.path.name

  var inventoryItem: InventoryItem = InventoryItem.empty

  def receiveCommand = {
    case cmd: Command =>
      try {
        val event = cmd match {
          case SetProduct(i) => ProductChanged(i)
          case HoldItem(i, sc, c) => ItemHeld(i, sc, c)
          case MakeReservation(i, r, c) => ReservationMade(i, r, c)
          case Checkout(i, sc, p) => CheckedOut(i, sc, p)
        }
        inventoryItem = inventoryItem.applyEvent(event)
        persist(event) {_ =>
          sender() ! event
        }
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
  }

  def receiveRecover = {
    case e: Event => inventoryItem = inventoryItem.applyEvent(e)
  }

}



