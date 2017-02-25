package com.ecommerce.orchestrator.backend.clientapi

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.{InventoryHttpClient, HttpClient}
import com.ecommerce.common.clientactors.kafka.InventoryKafkaClient
import com.ecommerce.common.clientactors.protocols.InventoryProtocol
import com.ecommerce.common.views.{InventoryRequest, InventoryResponse}
import com.ecommerce.common.identity.Identity

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lukewyman on 2/8/17.
  */
trait InventoryApi { this: Actor =>
  import HttpClient._
  import InventoryProtocol._
  import InventoryRequest._
  import InventoryResponse._
  import Identity._
  import akka.pattern.ask

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def inventoryClient = context.actorOf(InventoryHttpClient.props)
  def inventoryQueue = context.actorOf(InventoryKafkaClient.props)

  def getInventoryItem(productID: ProductRef): Future[HttpClientResult[InventoryItemView]] =
    inventoryClient.ask(GetItem(productID)).mapTo[HttpClientResult[InventoryItemView]]

  def notifySupply(productId: ProductRef, shipmentId: ShipmentRef,
                   expectedDelivery: ZonedDateTime, count: Int): Future[HttpClientResult[InventoryItemView]] =
    inventoryClient.ask(NotifySupply(productId, shipmentId, expectedDelivery, count)).mapTo[HttpClientResult[InventoryItemView]]

  def receiveSupply(productId: ProductRef, shipmentId: ShipmentRef, delivered: ZonedDateTime, count: Int): Future[HttpClientResult[InventoryItemView]] =
    inventoryClient.ask(ReceiveSupply(productId,shipmentId, delivered, count)).mapTo[HttpClientResult[InventoryItemView]]

  def releaseInventory(shoppingCartId: ShoppingCartRef, productId: ProductRef) =
    inventoryQueue ! ReleaseItem(productId, shoppingCartId)

  def holdInventory(shoppingCartId: ShoppingCartRef, productId: ProductRef, count: Int): Future[HttpClientResult[HoldItemView]] =
    inventoryClient.ask(HoldItem(productId, shoppingCartId, count)).mapTo[HttpClientResult[HoldItemView]]

  def claimInventory(shoppingCartId: ShoppingCartRef, productId: ProductRef, paymentId: PaymentRef): Future[HttpClientResult[ClaimItemView]] =
    inventoryQueue.ask(ClaimItem(productId, shoppingCartId, paymentId)).mapTo[HttpClientResult[ClaimItemView]]
}
