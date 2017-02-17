package com.ecommerce.inventory.backend.domain

import java.time.ZonedDateTime

import com.ecommerce.common.identity.Identity.ShipmentRef

/**
  * Created by lukewyman on 2/16/17.
  */
case class Shipment(shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime, delivered: ZonedDateTime, count: Int)
