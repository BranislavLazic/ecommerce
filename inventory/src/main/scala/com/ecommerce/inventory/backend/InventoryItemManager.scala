package com.ecommerce.inventory.backend

import akka.actor.Props
import akka.persistence.PersistentActor
import com.ecommerce.common.identity.Identity._
import com.ecommerce.inventory.backend.domain.{Reservation, Shipment, InventoryItem}

/**
  * Created by lukewyman on 12/18/16.
  */
object InventoryItemManager {

  def props = Props(new InventoryItemManager)

  def namei(pr: ProductRef) = pr.id.toString

  trait InventoryMessage {
    def item: ProductRef
  }

  sealed trait Command extends InventoryMessage
  case class SetProduct(item: ProductRef) extends Command
  case class HoldItem(item: ProductRef, shoppingCart: ShoppingCartRef, count: Int) extends Command
  case class MakeReservation(item: ProductRef, reservation: Reservation, count: Int) extends Command
  case class Checkout(item: ProductRef, shoppingCart: ShoppingCartRef, payment: PaymentRef) extends Command
  case class AbandonCart(item: ProductRef, shoppingCart: ShoppingCartRef) extends Command
  case class ReceiveSupply(item: ProductRef, shipment: Shipment) extends Command
  case class NotifySupply(item: ProductRef, shipment: Shipment) extends Command

  sealed trait Event extends InventoryMessage with Serializable
  case class ProductChanged(item: ProductRef) extends Event
  case class ItemHeld(item: ProductRef, shoppingCart: ShoppingCartRef, count: Int) extends Event
  case class ReservationMade(item: ProductRef, reservation: Reservation, count: Int) extends Event
  case class CheckedOut(item: ProductRef, shoppingCart: ShoppingCartRef, payment: PaymentRef) extends Event
  case class CartAbandoned(item: ProductRef, shoppingCart: ShoppingCartRef, customer: CustomerRef) extends Event
  case class ShipmentAccepted(item: ProductRef, shipment: Shipment) extends Event
  case class ShipmentAcknowledged(item: ProductRef, shipment: Shipment) extends Event

  case class Rejection(reason: String)

  sealed trait Query extends InventoryMessage
  case class GetItem(item: ProductRef) extends Query

  sealed trait Result extends InventoryMessage
  case class GetItemResult(item: ProductRef) extends Result
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



