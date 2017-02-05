package com.ecommerce.ordertracking.api

import java.util.UUID

/**
  * Created by lukewyman on 2/5/17.
  */
object RequestViews {

  case class CreateOrderView(orderId: UUID)
  case class UpdateOrderItemView(orderId: UUID, orderItemId: UUID)
}

object ResponseViews {

  case class OrderView(orderId: UUID)

}
