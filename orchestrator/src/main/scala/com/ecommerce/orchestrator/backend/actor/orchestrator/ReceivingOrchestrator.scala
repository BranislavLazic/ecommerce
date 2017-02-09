package com.ecommerce.orchestrator.backend.actor.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Props, Actor}
import akka.util.Timeout
import cats.Monad
import cats.data.EitherT
import cats.implicits._
import com.ecommerce.common.clientactors.http._
import com.ecommerce.common.clientactors.kafka.InventoryKafkaClient
import scala.concurrent.duration._

/**
  * Created by lukewyman on 2/6/17.
  */
object ReceivingOrchestrator {

  val props = Props(new ReceivingOrchestrator)

  val name = "receiving-orchestrator"

  case class PlaceOrder(productId: UUID, count: Int)
  case class AcknowledgeShipment(shipmentId: UUID, expectedDelivery: ZonedDateTime)
  case class AcceptShipment(shipmentId: UUID)
}

class ReceivingOrchestrator extends Actor
  with ReceivingApi {
  import akka.pattern.pipe
  import ReceivingOrchestrator._

  implicit def executionContext = context.dispatcher
  implicit def timeout: Timeout = Timeout(3 seconds)

  val receivingClient = context.actorOf(ReceivingHttpClient.props, ReceivingHttpClient.name)
  val inventoryClient = context.actorOf(InventoryHttpClient.props, InventoryHttpClient.name)
  val inventoryQueue = context.actorOf(InventoryKafkaClient.props, InventoryKafkaClient.name)

  def receive = {
    case PlaceOrder(pid, c) =>
      createShipment(pid, c).pipeTo(sender())
    case AcknowledgeShipment(sid, ed) =>
      acknowledgeShipment(sid, ed).pipeTo(sender())
    case AcceptShipment(sid) =>
      acceptShipment(sid).pipeTo(sender())
  }
}