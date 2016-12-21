package com.ecommerce.inventory.backend

import java.util.UUID
import org.joda.time.DateTime
import Stock._

/**
  * Created by lukewyman on 12/11/16.
  */
case class Stock(product: Option[ItemRef], count: Int, onHold: Map[ShoppingCartRef, Int]) {

  def setProduct(item: ItemRef): Stock = {
    require(product.isEmpty, "product cannot be overwritten")
    copy(product = Some(item))
  }

  def acceptShipment(shipment: ShipmentRef): Stock = {
    copy(count = shipment.count)
  }

  def holdForCustomer(shoppingCart: ShoppingCartRef, count: Int): Stock = {
    copy(onHold = onHold.updated(shoppingCart, count))
  }

  def abandonCart(shoppingCart: ShoppingCartRef): Stock = {
    copy(onHold = onHold.filterNot({case (scr, _) => scr.id == shoppingCart.id}))
  }

  def checkout(shoppingCart: ShoppingCartRef): Stock = {
    val holdCount = onHold.get(shoppingCart).getOrElse(0)
    copy(count = count - holdCount, onHold = onHold.filterNot({case (scr, _) => scr.id == shoppingCart.id}))
  }

  def availableCount: Int = {
    count - onHold.values.sum
  }
}

object Stock {
  def empty = Stock(None, 0, Map.empty)

  case class ItemRef(id: UUID)
  case class ShipmentRef(id: UUID, expectedDate: DateTime, count: Int)
  case class ShoppingCartRef(id: UUID)
  case class CustomerRef(id: UUID)
  case class Reservation(customer: CustomerRef, shipmentRef: ShipmentRef)
}
