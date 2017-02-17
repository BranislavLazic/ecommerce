package com.ecommerce.inventory.backend.domain

import com.ecommerce.common.identity.Identity._

/**
  * Created by lukewyman on 12/11/16.
  */
case class Stock(inStock: Int, onHold: Map[ShoppingCartRef, Int]) {

  def acceptShipment(shipment: Shipment): Stock = {
    copy(inStock = shipment.count)
  }

  def placeHold(shoppingCart: ShoppingCartRef, count: Int): Stock = {
    require(inStock >= count, s"inStock of $inStock is not sufficient to cover Hold of count $count")
    copy(onHold = onHold.updated(shoppingCart, count))
  }

  def releaseHold(shoppingCart: ShoppingCartRef): Stock = {
    copy(onHold = onHold.filterNot({ case (scr, _) => scr.id == shoppingCart.id }))
  }

  def claimItem(shoppingCart: ShoppingCartRef): Stock = {
    require(onHold.contains(shoppingCart), s"no Hold to claim for ShoppingCart $shoppingCart")
    val holdCount = onHold.get(shoppingCart).getOrElse(0)
    copy(inStock = inStock - holdCount, onHold = onHold.filterNot({ case (scr, _) => scr.id == shoppingCart.id }))
  }

  def availableCount: Int = {
    inStock - onHold.values.sum
  }

}

object Stock {
  def empty = Stock(0, Map.empty)
}
