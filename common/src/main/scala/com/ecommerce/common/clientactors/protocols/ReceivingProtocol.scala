package com.ecommerce.common.clientactors.protocols

import java.time.ZonedDateTime
import java.util.UUID

import com.ecommerce.common.identity.Identity._

/**
  * Created by lukewyman on 2/6/17.
  */
object ReceivingProtocol {

  case class CreateShipment(productId: ProductRef, ordered: ZonedDateTime, count: Int)
  case class GetShipment(shipmentId: ShipmentRef)
  case class AcknowledgeShipment(shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime)
  case class AcceptShipment(shipmentId: ShipmentRef)
}
