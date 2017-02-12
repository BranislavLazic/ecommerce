package com.ecommerce.orchestrator.backend.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ActorLogging, Actor, Props}
import akka.util.Timeout
import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import com.ecommerce.common.clientactors.http.HttpClient.HttpClientError
import com.ecommerce.common.clientactors.http._
import com.ecommerce.common.clientactors.kafka.InventoryKafkaClient
import com.ecommerce.orchestrator.backend.Mappers
import com.ecommerce.orchestrator.backend.clientapi.{ReceivingApi, InventoryApi}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by lukewyman on 2/6/17.
  */
object ReceivingOrchestrator {

  val props = Props(new ReceivingOrchestrator)

  val name = "receiving-orchestrator"

  case class GetShipmentSummary(shipmentId: UUID)
  case class RequestShipment(productId: UUID, ordered: ZonedDateTime, count: Int)
  case class AcknowledgeShipment(itemId: UUID, shipmentId: UUID, expectedDelivery: ZonedDateTime, count: Int)
  case class AcceptShipment(itemId: UUID, shipmentId: UUID, delivered: ZonedDateTime, count: Int)
}

class ReceivingOrchestrator extends Actor with ActorLogging
  with ReceivingApi
  with InventoryApi {
  import Mappers._
  import ReceivingOrchestrator._
  import akka.pattern.pipe

  implicit def executionContext = context.dispatcher
  implicit def timeout: Timeout = Timeout(3 seconds)

  val receivingClient = context.actorOf(ReceivingHttpClient.props, ReceivingHttpClient.name)
  val inventoryClient = context.actorOf(InventoryHttpClient.props, InventoryHttpClient.name)
  val inventoryQueue = context.actorOf(InventoryKafkaClient.props, InventoryKafkaClient.name)

  def receive = {
    case GetShipmentSummary(sid) =>
      val result = for {
        gs <- EitherT(getShipment(sid))
        gi <- EitherT(getInventoryItem(gs.productId))
      } yield mapToReceivingSummaryView(gs, gi)
      result.value.pipeTo(sender())
      kill()
    case RequestShipment(pid, o, c) =>
      val cs = EitherT(createShipment(pid, c))
      val gi = EitherT(getInventoryItem(pid))
      Applicative[EitherT[Future, HttpClientError, ?]].map2(cs, gi)(mapToReceivingSummaryView)
        .value.pipeTo(sender())
      kill()
    case AcknowledgeShipment(iid, sid, ed, c) =>
      val as = EitherT(acknowledgeShipment(sid, ed))
      val ns = EitherT(notifySupply(iid, sid, ed, c))
      Applicative[EitherT[Future, HttpClientError, ?]].map2(as, ns)(mapToReceivingSummaryView)
        .value.pipeTo(sender())
      kill()
    case AcceptShipment(iid, sid, d, c) =>
      val as = EitherT(acceptShipment(sid))
      val rs = EitherT(receiveSupply(iid, sid, d, c))
      Applicative[EitherT[Future, HttpClientError, ?]].map2(as, rs)(mapToReceivingSummaryView)
        .value.pipeTo(sender())
      kill()
  }

  def kill() = log.info("should kill children and self here!") // TODO: implementation to kill http cleint actors and self
}