package com.ecommerce.common.views

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 2/5/17.
  */
object InventoryRequest {
  case class CreateItemView(itemId: UUID)
  case class ReceiveSupplyView(shipmentId: UUID, date: ZonedDateTime, count: Int)
  case class NotifySupplyView(shipmentId: UUID, expectedDate: ZonedDateTime, count: Int)
  case class HoldItemView(count: Int)
  case class ReserveItemView(customerId: UUID, shipmentId: UUID, count: Int)
  case class CheckoutView(creditCard: String)
  case class ClaimItemView(shoppingCartId: UUID, itemId: UUID)
}

object InventoryResponse {
  case class InventoryItemView(productId: UUID, inStock: Int, onBackorder: Int)
}
