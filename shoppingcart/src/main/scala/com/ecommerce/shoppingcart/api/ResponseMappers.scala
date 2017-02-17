package com.ecommerce.shoppingcart.api

import java.util.UUID

import com.ecommerce.common.identity.Identity.{ProductRef, ShoppingCartRef}
import com.ecommerce.shoppingcart.backend.ShoppingCart
import com.ecommerce.common.views.ShoppingCartResponse
import ShoppingCart._

/**
  * Created by lukewyman on 12/13/16.
  */

object ResponseMappers {
  import ShoppingCartResponse._

  def mapToShoppingCartView(id: ShoppingCartRef, sc: ShoppingCart): ShoppingCartView =
    ShoppingCartView(id.id, sc.owner.map(_.id), mapToIItemViews(sc.items))

  def mapToIItemViews(items: Map[ProductRef, Int]): List[ShoppingCartItemView] =
    items.map { case (itemRef, count) => ShoppingCartItemView(itemRef.id, count) }.toList
}
