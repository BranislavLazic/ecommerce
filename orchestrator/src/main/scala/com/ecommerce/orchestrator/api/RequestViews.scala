package com.ecommerce.orchestrator.api

import java.util.UUID

/**
  * Created by lukewyman on 1/1/17.
  */
object RequestViews {

  case class CheckoutView(shoppingcartId: UUID, shippingAddress: String, creditCard: String)

}
