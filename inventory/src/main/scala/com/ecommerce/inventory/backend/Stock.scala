package com.ecommerce.inventory.backend

import java.util.UUID
import com.ecommerce.inventory.backend.StockMessage._
import org.joda.time.DateTime
import Identity._

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

  def applyEvent(event: Event): Stock = event match {
    case ProductChanged(i) => setProduct(i)
    case ShipmentAccepted(_, s) => acceptShipment(s)
    case HeldForCustomer(_, sc, c) => holdForCustomer(sc, c)
    case CartAbandoned(_, sc) => abandonCart(sc)
    case CheckedOut(_, sc) => checkout(sc)
  }
}

object Stock {
  def empty = Stock(None, 0, Map.empty)
}
