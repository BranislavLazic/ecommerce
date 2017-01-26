package com.ecommerce.inventory.backend.domain

import com.ecommerce.inventory.backend.InventoryItemManager._
import com.ecommerce.inventory.backend.domain.Identity.ItemRef

/**
  * Created by lukewyman on 1/4/17.
  */
case class InventoryItem(product: Option[ItemRef], stock: Stock, backorder: Backorder) {

  def setProduct(item: ItemRef): InventoryItem = {
    require(product.isEmpty, "product cannot be overwritten")
    copy(product = Some(item))
  }

  def applyEvent(event: Event): InventoryItem = event match {
    case ProductChanged(i) => setProduct(i)
    case ItemHeld(_, sh, c) => copy(stock = stock.placeHold(sh, c))
    case CheckedOut(_, sc, _) => copy(stock = stock.claimItem(sc))
    case CartAbandoned(_, sc, c) => copy(stock = stock.releaseHold(sc), backorder = backorder.releaseReservation(c))
    case ShipmentAccepted(_, sh) => copy(stock = stock.acceptShipment(sh), backorder = backorder.acceptShipment(sh))
    case ShipmentAcknowledged(_, sh) => copy(backorder = backorder.acknowledgeShipment(sh))
  }
}

object InventoryItem {

  def empty = InventoryItem(None, Stock.empty, Backorder.empty)
}