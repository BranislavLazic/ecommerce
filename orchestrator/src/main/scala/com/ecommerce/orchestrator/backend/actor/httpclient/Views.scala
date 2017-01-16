package com.ecommerce.orchestrator.backend.actor.httpclient

import java.util.UUID

/**
  * Created by lukewyman on 1/15/17.
  */
object RequestViews {
  sealed trait RequestView
  case class CreateShoppingCartView(shoppingCartId: UUID, customerId: UUID) extends RequestView
  case class AddItemView(count: Int) extends RequestView
}

object ResponseViews {
  sealed trait ResponseView
  case class ShoppingCartView(shoppingCartId: UUID) extends ResponseView
}
