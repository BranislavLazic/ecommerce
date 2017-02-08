package com.ecommerce.receiving.backend

import java.time.ZonedDateTime

import Shipment._
/**
  * Created by lukewyman on 2/7/17.
  */
trait ReceivingMessage {
  def shipmentId: ShipmentRef
}

sealed trait Command extends ReceivingMessage
case class SetProductAndCount(shipmentId: ShipmentRef, product: ProductRef, count: Int) extends Command
case class UpdateExpectedDelivery(shipmentId: ShipmentRef, expecedDelivery: ZonedDateTime) extends Command
case class ReceiveDelivery(shipmentId: ShipmentRef, delivery: ZonedDateTime) extends Command

sealed trait Event extends ReceivingMessage
case class ProductAndCountChanged(shipmentId: ShipmentRef, product: ProductRef, count: Int) extends Event
case class ExpectedDeliveryUpdated(shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime) extends Event
case class DeliveryReceived(shipmentId: ShipmentRef, delivery: ZonedDateTime) extends Event

sealed trait Query extends ReceivingMessage
case class GetShipment(shipmentId: ShipmentRef) extends Query

sealed trait ManagerResponse

sealed trait Result extends ReceivingMessage with ManagerResponse
case class GetShipmentResult(shipmentId: ShipmentRef, shipment: Shipment) extends Result

case class Rejection(reason: String) extends ManagerResponse

