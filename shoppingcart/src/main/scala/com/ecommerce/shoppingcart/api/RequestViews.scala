package com.ecommerce.shoppingcart.api

import java.util.UUID

import com.ecommerce.shoppingcart.backend.{GetItemsResult, ShoppingCart}
import ShoppingCart._

/**
  * Created by lukewyman on 12/13/16.
  */
object RequestViews {

  case class CreateShoppingCartView(shoppingCartId: UUID, customerId: UUID)
  case class RemoveItemView(itemId: UUID, count: Int)
  case class AddItemCountView(count: Int)
}

object ResponseViews {
  case class ShoppingCartView(id: UUID, items: List[ItemView])
  case class ItemView(id: UUID, count: Int)
}

object ResponseMappers {
  import ResponseViews._

  def mapToShoppingCartView(gir: GetItemsResult): ShoppingCartView =
    ShoppingCartView(gir.shoppingCart.id, mapToIItemViews(gir.items))

  def mapToIItemViews(items: Map[ItemRef, Int]): List[ItemView] =
    items.map { case (itemRef, count) => ItemView(itemRef.id, count) }.toList
}
