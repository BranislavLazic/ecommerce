package com.ecommerce.common.clientactors.protocols

import java.util.UUID

import com.ecommerce.common.identity.Identity._

/**
  * Created by lukewyman on 2/5/17.
  */
object ShoppingCartProtocol {

  case class GetShoppingCart(shoppingCartId: ShoppingCartRef)
  case class CreateShoppingCart(shoppingCartId: ShoppingCartRef, customerId: CustomerRef)
  case class AddItem(shoppingCartId: ShoppingCartRef, productId: ProductRef, count: Int)
  case class RemoveItem(shoppingCartId: ShoppingCartRef, productId: ProductRef)
  case class ClearCart(shoppingCartId: ShoppingCartRef)
}
