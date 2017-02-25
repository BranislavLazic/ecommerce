package com.ecommerce.orchestrator.backend.clientapi

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.{ReceivingHttpClient, HttpClient}
import com.ecommerce.common.clientactors.protocols.ReceivingProtocol
import com.ecommerce.common.identity.Identity.{ProductRef, ShipmentRef}
import com.ecommerce.common.views.ReceivingResponse

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lukewyman on 2/8/17.
  */
trait ReceivingApi { this: Actor =>

  import HttpClient._
  import ReceivingProtocol._
  import ReceivingResponse._
  import akka.pattern.ask

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  val receivingClient = context.actorOf(ReceivingHttpClient.props)

  def getShipment(shipmentId: ShipmentRef): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(GetShipment(shipmentId)).mapTo[HttpClientResult[ShipmentView]]

  def createShipment(productId: ProductRef, ordered: ZonedDateTime, count: Int): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(CreateShipment(productId, ordered, count)).mapTo[HttpClientResult[ShipmentView]]

  def acknowledgeShipment(shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(AcknowledgeShipment(shipmentId, expectedDelivery)).mapTo[HttpClientResult[ShipmentView]]

  def acceptShipment(shipmentId: ShipmentRef): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(AcceptShipment(shipmentId)).mapTo[HttpClientResult[ShipmentView]]
}
