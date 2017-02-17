package com.ecommerce.common.clientactors.protocols

import java.time.ZonedDateTime
import java.util.UUID

import com.ecommerce.common.identity.Identity._

/**
  * Created by lukewyman on 2/5/17.
  */
object InventoryProtocol {

  case class CreateItem(productId: ProductRef)
  case class GetItem(productId: ProductRef)
  case class ReceiveSupply(productId: ProductRef, shipmentId: ShipmentRef, date: ZonedDateTime, count: Int)
  case class NotifySupply(productId: ProductRef, shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime, count: Int)
  case class HoldItem(productId: ProductRef, shoppingCartId: ShoppingCartRef, count: Int)
  case class ReserveItem(productId: ProductRef, customerId: CustomerRef, count: Int)
  case class ReleaseItem(productId: ProductRef, shoppingCartId: ShoppingCartRef)
  case class ClaimItem(productId: ProductRef, shoppingCartId: ShoppingCartRef, paymentId: PaymentRef)
}
