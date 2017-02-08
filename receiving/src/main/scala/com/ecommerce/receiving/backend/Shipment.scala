package com.ecommerce.receiving.backend

import java.time.ZonedDateTime
import java.util.UUID
import Shipment._

/**
  * Created by lukewyman on 2/7/17.
  */
case class Shipment(product: Option[ProductRef], count: Int, expectedDelivery: Option[ZonedDateTime], delivered: Option[ZonedDateTime]) {

  def setProductAndCount(productId: ProductRef, count: Int): Shipment = {
    require(product.isEmpty, "product cannot be overwritten")
    copy(product = Some(productId), count = count)
  }

  def setExpectedDelivery(date: ZonedDateTime): Shipment = {
    copy(expectedDelivery = Some(date))
  }

  def setDelivered(date: ZonedDateTime): Shipment = {
    copy(delivered = Some(date))
  }

  def applyEvent(event: Event): Shipment = event match {
    case ProductAndCountChanged(_, p, c) => setProductAndCount(p, c)
    case ExpectedDeliveryUpdated(_, ed) => setExpectedDelivery(ed)
    case DeliveryReceived(_, d) => setDelivered(d)
  }
}

object Shipment {
  def empty: Shipment = Shipment(None, 0, None, None)

  case class ShipmentRef(id: UUID)
  case class ProductRef(id: UUID)
}
