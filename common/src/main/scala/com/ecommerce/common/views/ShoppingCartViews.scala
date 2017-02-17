package com.ecommerce.common.views

import java.util.UUID

/**
  * Created by lukewyman on 2/5/17.
  */
object ShoppingCartRequest {
  case class CreateShoppingCartView(shoppingCartId: UUID, customerId: UUID)
  case class AddItemView(count: Int, backorder: Boolean)
}

object ShoppingCartResponse {
  case class ShoppingCartView(shoppingCartId: UUID, customerId: Option[UUID],  items: List[ShoppingCartItemView])
  case class ShoppingCartItemView(productId: UUID, count: Int)
}
