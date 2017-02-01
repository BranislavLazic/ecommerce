package com.ecommerce.shoppingcart.api

import java.util.UUID

import com.ecommerce.shoppingcart.backend.{GetShoppingCartResult, ShoppingCart}
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
  case class ShoppingCartView(shoppingCartId: UUID, customerId: Option[UUID], items: List[ItemView])
  case class ItemView(id: UUID, count: Int)
}

object ResponseMappers {
  import ResponseViews._

  def mapToShoppingCartView(id: ShoppingCartRef, sc: ShoppingCart): ShoppingCartView =
    ShoppingCartView(id.id, sc.owner.map(_.id), mapToIItemViews(sc.items))

  def mapToIItemViews(items: Map[ItemRef, Int]): List[ItemView] =
    items.map { case (itemRef, count) => ItemView(itemRef.id, count) }.toList
}
