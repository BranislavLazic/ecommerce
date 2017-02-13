package com.ecommerce.common.views

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 2/6/17.
  */

object ReceivingRequest {

  case class CreateShipmentView(productId: UUID, ordered: ZonedDateTime, count: Int)
  case class AcknowledgeShipmentView(expectedDelivery: ZonedDateTime)
}

object ReceivingResponse {

  case class ShipmentView(
                           shipmentId: UUID,
                           productId: UUID,
                           ordered: ZonedDateTime,
                           expectedDelivery: ZonedDateTime,
                           delivered: ZonedDateTime,
                           count: Int)
}


