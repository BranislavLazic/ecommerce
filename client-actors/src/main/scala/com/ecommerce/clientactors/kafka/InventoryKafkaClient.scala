package com.ecommerce.clientactors.kafka

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 1/22/17.
  */
object InventoryKafkaClient {

  val props = Props(new InventoryKafkaClient)

  val name = "inventory-kafka-client"

  case class CreateItem(itemId: UUID)
  case class GetItem(itemId: UUID)
  case class AcceptShipment(itemId: UUID, shipmentId: UUID, date: ZonedDateTime, count: Int)
  case class AcknowledgeShipment(itemId: UUID, shipmentId: UUID, expectedDate: ZonedDateTime, count: Int)
  case class HoldItem(itemId: UUID, shoppingCartId: UUID, count: Int)
  case class ReserveItem(itemId: UUID, customerId: UUID, count: Int)
  case class ReleaseItem(itemId: UUID, shoppingCartId: UUID)
  case class ClaimItem(itemId: UUID, shoppingCartId: UUID, paymentId: UUID)
}

class InventoryKafkaClient extends Actor {

  def receive = ???

}
