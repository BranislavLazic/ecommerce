package com.ecommerce.receiving.api

import java.time.ZonedDateTime
import java.util.UUID

import com.ecommerce.common.views.ReceivingResponse.ShipmentView
import com.ecommerce.receiving.backend.Shipment
import com.ecommerce.receiving.backend.Shipment.ShipmentRef

/**
  * Created by lukewyman on 2/7/17.
  */
object ResponseMappers {

  def mapToShipmentView(shipmentId: ShipmentRef, shipment: Shipment): ShipmentView =
    ShipmentView(
      shipmentId.id,
      shipment.product.fold(null.asInstanceOf[UUID])(_.id),
      ZonedDateTime.now,
      shipment.expectedDelivery.getOrElse(null),
      shipment.delivered.getOrElse(null),
      shipment.count
    )

}
