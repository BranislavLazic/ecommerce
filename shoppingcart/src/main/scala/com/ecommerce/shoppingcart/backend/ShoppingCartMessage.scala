package com.ecommerce.shoppingcart.backend

import ShoppingCart._
/**
  * Created by lukewyman on 12/11/16.
  */
trait ShoppingCartMessage {
  def shoppingCart: ShoppingCartRef
}

sealed trait Command extends ShoppingCartMessage
case class SetOwner(shoppingCart: ShoppingCartRef, owner: CustomerRef) extends Command
case class AddItem(shoppingCart: ShoppingCartRef, item: ItemRef, count: Int) extends Command
case class RemoveItem(shoppingCart: ShoppingCartRef, item: ItemRef) extends Command

sealed trait Event extends ShoppingCartMessage with Serializable
case class OwnerChanged(shoppingCart: ShoppingCartRef, owner: CustomerRef) extends Event
case class ItemAdded(shoppingCart: ShoppingCartRef, item: ItemRef, count: Int) extends Event
case class ItemRemoved(shoppingCart: ShoppingCartRef, item: ItemRef) extends Event

sealed trait Query extends ShoppingCartMessage
case class GetItems(shoppingCart: ShoppingCartRef) extends Query

sealed trait Result extends ShoppingCartMessage
case class GetItemsResult(shoppingCart: ShoppingCartRef, items: Map[ItemRef, Int]) extends Result