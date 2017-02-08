package com.ecommerce.common.views

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 2/6/17.
  */

object ReceivingRequest {

  case class CreateShipmentView(shipmentId: UUID, productId: UUID, count: Int)
  case class AcknowledgeShipmentView(expectedDelivery: ZonedDateTime)
}

object ReceivingResponse {

  case class ShipmentView(shipmentId: UUID, productId: UUID, expectedDelivery: ZonedDateTime, count: Int)
}


