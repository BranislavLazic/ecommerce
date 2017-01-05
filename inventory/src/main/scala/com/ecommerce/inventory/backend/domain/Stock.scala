package com.ecommerce.inventory.backend.domain

import Identity.{ShipmentRef, ShoppingCartRef}

/**
  * Created by lukewyman on 12/11/16.
  */
case class Stock(count: Int, onHold: Map[ShoppingCartRef, Int]) {

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
  def empty = Stock(0, Map.empty)
}
