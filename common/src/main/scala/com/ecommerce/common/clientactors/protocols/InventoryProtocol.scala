package com.ecommerce.common.clientactors.protocols

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 2/5/17.
  */
object InventoryProtocol {

  case class CreateItem(itemId: UUID)
  case class GetItem(itemId: UUID)
  case class ReceiveSupply(itemId: UUID, shipmentId: UUID, date: ZonedDateTime, count: Int)
  case class NotifySupply(itemId: UUID, shipmentId: UUID, expectedDate: ZonedDateTime, count: Int)
  case class HoldItem(itemId: UUID, shoppingCartId: UUID, count: Int)
  case class ReserveItem(itemId: UUID, customerId: UUID, count: Int)
  case class ReleaseItem(itemId: UUID, shoppingCartId: UUID)
  case class ClaimItem(itemId: UUID, shoppingCartId: UUID, paymentId: UUID)
}
