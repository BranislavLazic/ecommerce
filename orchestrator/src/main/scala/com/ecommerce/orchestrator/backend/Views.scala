package com.ecommerce.orchestrator.backend

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 2/8/17.
  */

object RequestViews {

  // Receiving
  case class RequestShipmentView(productId: UUID, ordered: ZonedDateTime, count: Int)
  case class AcknowledgeShipmentView(shipmentId: UUID, productId: UUID, expectedDelivery: ZonedDateTime, count: Int)
  case class AcceptShipmentView(shipmentId: UUID, productId: UUID, delivered: ZonedDateTime, count: Int)

  // Shopping
  case class CheckoutView(shoppingCartId: UUID, creditCard: String)
}

object ResponseViews {

  case class ReceivingSummaryView(
                                   productId: UUID,
                                   shipmentId: UUID,
                                   dateOrdered: ZonedDateTime,
                                   amountOrdered: Int,
                                   expectedDelivery: ZonedDateTime,
                                   delivered: ZonedDateTime,
                                   inStock: Int,
                                   onBackorder: Int
                                 )

  case class ProductSummaryView(displayName: String)

}


