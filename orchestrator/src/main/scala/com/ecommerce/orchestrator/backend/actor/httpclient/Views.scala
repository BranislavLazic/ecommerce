package com.ecommerce.orchestrator.backend.actor.httpclient

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 1/15/17.
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
}

object ResponseViews {
  sealed trait ResponseView
  case class ShoppingCartView(shoppingCartId: UUID) extends ResponseView
  case class InventoryItemView(ItemId: UUID) extends ResponseView
}
