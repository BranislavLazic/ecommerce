package com.ecommerce.shoppingcart.backend

import ShoppingCart._
/**
  * Created by lukewyman on 12/11/16.
  */
trait ShoppingCartMessage {
  def shoppingCartId: ShoppingCartRef
}

sealed trait Command extends ShoppingCartMessage
case class SetOwner(shoppingCartId: ShoppingCartRef, owner: CustomerRef) extends Command
case class AddItem(shoppingCartId: ShoppingCartRef, item: ItemRef, count: Int) extends Command
case class RemoveItem(shoppingCartId: ShoppingCartRef, item: ItemRef) extends Command

sealed trait Event extends ShoppingCartMessage with Serializable
case class OwnerChanged(shoppingCartId: ShoppingCartRef, owner: CustomerRef) extends Event
case class ItemAdded(shoppingCartId: ShoppingCartRef, item: ItemRef, count: Int) extends Event
case class ItemRemoved(shoppingCartId: ShoppingCartRef, item: ItemRef) extends Event

sealed trait Query extends ShoppingCartMessage
case class GetItems(shoppingCartId: ShoppingCartRef) extends Query

sealed trait ManagerResponse

sealed trait Result extends ShoppingCartMessage with ManagerResponse
case class GetShoppingCartResult(shoppingCartId: ShoppingCartRef, shoppingCart: ShoppingCart) extends Result

case class Rejection(reason: String) extends ManagerResponse