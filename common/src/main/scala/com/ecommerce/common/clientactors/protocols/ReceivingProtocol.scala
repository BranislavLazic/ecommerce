package com.ecommerce.common.clientactors.protocols

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 2/6/17.
  */
object ReceivingProtocol {

  case class CreateShipment(productId: UUID, count: Int)
  case class GetShipment(shipmentId: UUID)
  case class AcknowledgeShipment(shipmentId: UUID, expectedDelivery: ZonedDateTime)
  case class AcceptShipment(shipmentId: UUID)
}
