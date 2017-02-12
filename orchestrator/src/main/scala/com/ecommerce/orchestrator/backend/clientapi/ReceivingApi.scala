package com.ecommerce.orchestrator.backend.clientapi

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.ActorRef
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient
import com.ecommerce.common.clientactors.protocols.ReceivingProtocol
import com.ecommerce.common.views.ReceivingResponse

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lukewyman on 2/8/17.
  */
trait ReceivingApi {

  import HttpClient._
  import ReceivingProtocol._
  import ReceivingResponse._
  import akka.pattern.ask

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def receivingClient: ActorRef

  def getShipment(shipmentId: UUID): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(GetShipment(shipmentId)).mapTo[HttpClientResult[ShipmentView]]

  def createShipment(productId: UUID, count: Int): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(CreateShipment(productId, count)).mapTo[HttpClientResult[ShipmentView]]

  def acknowledgeShipment(shipmentId: UUID, expectedDelivery: ZonedDateTime): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(AcknowledgeShipment(shipmentId, expectedDelivery)).mapTo[HttpClientResult[ShipmentView]]

  def acceptShipment(shipmentId: UUID): Future[HttpClientResult[ShipmentView]] =
    receivingClient.ask(AcceptShipment(shipmentId)).mapTo[HttpClientResult[ShipmentView]]
}
