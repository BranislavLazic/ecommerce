package com.ecommerce.orchestrator.backend.actor.orchestrator

import java.util.UUID

import akka.actor.ActorRef
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient
import com.ecommerce.common.clientactors.protocols.ShoppingCartProtocol
import com.ecommerce.common.views.ShoppingCartRequest
import com.ecommerce.common.views.ShoppingCartResponse

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by lukewyman on 2/8/17.
  */
trait ShoppingCartApi {
  import akka.pattern.ask
  import HttpClient._
  import ShoppingCartProtocol._
  import ShoppingCartRequest._
  import ShoppingCartResponse._

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def shoppingCartClient: ActorRef

  def createShoppingCart(shoppingCartId: UUID, customerId: UUID): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(CreateShoppingCart(shoppingCartId, customerId)).mapTo[HttpClientResult[ShoppingCartView]]

  def getShoppingCart(shoppingCartId: UUID): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(GetShoppingCart(shoppingCartId)).mapTo[HttpClientResult[ShoppingCartView]]

  def addItem(shoppingCartId: UUID, itemId: UUID, count: Int): Future[HttpClientResult[AddItemView]] =
    shoppingCartClient.ask(AddItem(shoppingCartId, itemId, count)).mapTo[HttpClientResult[AddItemView]]

  def removeItem(shoppingCartId: UUID, itemId: UUID): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(RemoveItem(shoppingCartId, itemId)).mapTo[HttpClientResult[ShoppingCartView]]

  def clearShoppingCart(shoppingCartId: UUID): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(ClearCart(shoppingCartId)).mapTo[HttpClientResult[ShoppingCartView]]
}
