package com.ecommerce.orchestrator.backend.clientapi

import java.util.UUID

import akka.actor.ActorRef
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient
import com.ecommerce.common.clientactors.protocols.ShoppingCartProtocol
import com.ecommerce.common.views.{ShoppingCartRequest, ShoppingCartResponse}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lukewyman on 2/8/17.
  */
trait ShoppingCartApi {
  import HttpClient._
  import ShoppingCartProtocol._
  import ShoppingCartRequest._
  import ShoppingCartResponse._
  import akka.pattern.ask

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
