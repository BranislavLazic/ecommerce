package com.ecommerce.inventory.backend

import Stock._

/**
  * Created by lukewyman on 12/21/16.
  */
trait StockMessage {
  def inventoryItem: ItemRef
}

object StockMessage {

  sealed trait Command extends StockMessage
  case class SetProduct(inventoryItem: ItemRef) extends Command
  case class AcceptShipment(inventoryItem: ItemRef, shipmentRef: ShipmentRef) extends Command
  case class HoldForCustomer(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef, count: Int) extends Command
  case class AbandonCart(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef) extends Command
  case class Checkout(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef) extends Command

  sealed trait Event extends StockMessage
  case class ProductChanged(inventoryItem: ItemRef) extends Event
  case class ShipmentAccepted(inventoryItem: ItemRef, shipment: ShipmentRef) extends Event
  case class HeldForCustomer(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef, count: Int) extends Event
  case class CartAbandoned(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef) extends Event
  case class CheckedOut(inventoryItem: ItemRef, shoppingCart: ShoppingCartRef) extends Event

  sealed trait Query extends StockMessage
  case class GetStock(inventoryItem: ItemRef) extends Query

  sealed trait Result extends StockMessage
  case class GetStockResult(inventoryItem: ItemRef) extends  Result
}