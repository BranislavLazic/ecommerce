package com.ecommerce.common.clientactors.protocols

import java.util.UUID

/**
  * Created by lukewyman on 2/5/17.
  */
object ShoppingCartProtocol {

  case class GetShoppingCart(id: UUID)
  case class CreateShoppingCart(shoppingCartId: UUID, customerId: UUID)
  case class AddItem(shoppingCartId: UUID, itemId: UUID, count: Int)
  case class RemoveItem(shoppingCartId: UUID, itemID: UUID)
  case class ClearCart(shoppingCartId: UUID)
}
