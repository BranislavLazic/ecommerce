package com.ecommerce.shoppingcart.backend

import java.util.UUID
import ShoppingCart._

/**
  * Created by lukewyman on 12/11/16.
  */
case class ShoppingCart(items: Map[ItemRef, Int], owner: Option[CustomerRef]) {

  def setOwner(customer: CustomerRef): ShoppingCart = {
    require(owner.isEmpty, "owner cannot be overwritten")
    copy(owner = Some(customer))
  }

  def addItem(item: ItemRef, count: Int): ShoppingCart = {
    require(count > 0, s"count must be positive - trying to add $item with $count")
    val currentCount = items.get(item).getOrElse(0)
    copy(items = items.updated(item, currentCount + count))
  }

  def removeItem(item: ItemRef, count: Int): ShoppingCart = {
    require(count > 0, s"count must be positive = trying to remove $item with $count")
    val currentCount = items.get(item).getOrElse(0)
    val newCount = currentCount - count
    if (newCount <= 0) copy(items = items - item)
    else copy(items = items.updated(item, newCount))
  }

  def applyEvent(event: Event): ShoppingCart = event match {
    case OwnerChanged(_, owner) => setOwner(owner)
    case ItemAdded(_, item, count) => addItem(item, count)
    case ItemRemoved(_, item, count) => removeItem(item, count)
  }
}

object ShoppingCart {
  def empty = ShoppingCart(Map.empty, None)

  case class ItemRef(id: UUID)
  case class CustomerRef(id: UUID)
  case class ShoppingCartRef(id: UUID)
}
