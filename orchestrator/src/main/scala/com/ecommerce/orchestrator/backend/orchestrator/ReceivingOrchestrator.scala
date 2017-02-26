package com.ecommerce.orchestrator.backend.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ActorLogging, Actor, Props}
import akka.util.Timeout
import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import com.ecommerce.common.clientactors.http.HttpClient.HttpClientError
import com.ecommerce.common.identity.Identity.{ProductRef, ShipmentRef}
import com.ecommerce.orchestrator.backend.Mappers
import com.ecommerce.orchestrator.backend.ResponseViews.ReceivingSummaryView
import com.ecommerce.orchestrator.backend.clientapi.{ReceivingApi, InventoryApi}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by lukewyman on 2/6/17.
  */
object ReceivingOrchestrator {

  val props = Props(new ReceivingOrchestrator)

  case class GetShipmentSummary(shipmentId: ShipmentRef)
  case class RequestShipment(productId: ProductRef, ordered: ZonedDateTime, count: Int)
  case class AcknowledgeShipment(productId: ProductRef, shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime, count: Int)
  case class AcceptShipment(productId: ProductRef, shipmentId: ShipmentRef, delivered: ZonedDateTime, count: Int)
}

class ReceivingOrchestrator extends Actor with ActorLogging
  with ReceivingApi
  with InventoryApi {
  import Mappers._
  import ReceivingOrchestrator._
  import akka.pattern.pipe

  implicit def executionContext = context.dispatcher
  implicit def timeout: Timeout = Timeout(3 seconds)

  def receive = {
    case GetShipmentSummary(sid) =>
      // Need Monads for control flow here, since getInventoryItem depends on the productId
      // retreived from getShipment
      val result: EitherT[Future, HttpClientError, ReceivingSummaryView] = for {
        gs <- EitherT(getShipment(sid))
        gi <- EitherT(getInventoryItem(ProductRef(gs.productId)))
      } yield mapToReceivingSummaryView(gs, gi)
      result.value.pipeTo(sender())
      kill()
      // The rest of these cases can use Applicative.map2, because the results are just being combined -
      // no control flow needed here.
    case RequestShipment(pid, o, c) =>
      val cs = EitherT(createShipment(pid, o, c))
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

  def kill() = {
    context.children foreach { context.stop(_) }
    context.stop(self)
  }

}