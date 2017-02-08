package com.ecommerce.orchestrator.backend.actor.orchestrator

import java.util.UUID

import akka.actor.ActorRef
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient
import com.ecommerce.common.clientactors.protocols.InventoryProtocol
import com.ecommerce.common.views.InventoryRequest

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by lukewyman on 2/8/17.
  */
trait InventoryApi {
  import akka.pattern.ask
  import HttpClient._
  import InventoryProtocol._
  import InventoryRequest._

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def inventoryClient: ActorRef
  def inventoryQueue: ActorRef

  def releaseInventory(shoppingCartId: UUID, itemId: UUID) =
    inventoryQueue ! ReleaseItem(itemId, shoppingCartId)

  def holdInventory(shoppingCartId: UUID, itemId: UUID, count: Int): Future[HttpClientResult[HoldItemView]] =
    inventoryClient.ask(HoldItem(shoppingCartId, itemId, count)).mapTo[HttpClientResult[HoldItemView]]

  def claimInventory(shoppingCartId: UUID, itemId: UUID, paymentId: UUID): Future[HttpClientResult[ClaimItemView]] =
    inventoryQueue.ask(ClaimItem(shoppingCartId, itemId, paymentId)).mapTo[HttpClientResult[ClaimItemView]]
}
