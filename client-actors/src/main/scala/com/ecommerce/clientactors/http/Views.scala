package com.ecommerce.clientactors.http

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 1/17/17.
  */
object RequestViews {
  sealed trait RequestView

  //Inventory
  case class CreateItemView(ItemId: UUID) extends RequestView
  case class AcceptShipmentView(shipmentId: UUID, date: ZonedDateTime, count: Int) extends RequestView
  case class AcknowledgeShhipmentView(shipmentId: UUID, expectedDate: ZonedDateTime, count: Int) extends RequestView
  case class HoldItemView(count: Int) extends RequestView
  case class ReserveItemView(customerId: UUID, count: Int) extends RequestView
  case class CheckoutView(paymentId: UUID) extends RequestView

  // ShoppingCart
  case class CreateShoppingCartView(shoppingCartId: UUID, customerId: UUID) extends RequestView
  case class AddItemView(count: Int) extends RequestView

  // Payment
  case class Payment()
}

object ResponseViews {
  sealed trait ResponseView
  case class ShoppingCartView(shoppingCartId: UUID, items: List[ShoppingCartItemView]) extends ResponseView
  case class ShoppingCartItemView(itemId: UUID, count: Int)
  case class InventoryItemView(ItemId: UUID) extends ResponseView
  case class PaymentTokenView(paymentId: UUID)
}