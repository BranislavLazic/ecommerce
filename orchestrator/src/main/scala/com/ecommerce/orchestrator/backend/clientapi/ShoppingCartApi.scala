package com.ecommerce.orchestrator.backend.clientapi

import java.util.UUID

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.{ShoppingCartHttpClient, HttpClient}
import com.ecommerce.common.clientactors.protocols.ShoppingCartProtocol
import com.ecommerce.common.identity.Identity.{ProductRef, ShoppingCartRef, CustomerRef}
import com.ecommerce.common.views.{ShoppingCartRequest, ShoppingCartResponse}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lukewyman on 2/8/17.
  */
trait ShoppingCartApi { this: Actor =>
  import HttpClient._
  import ShoppingCartProtocol._
  import ShoppingCartRequest._
  import ShoppingCartResponse._
  import akka.pattern.ask

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def shoppingCartClient = context.actorOf(ShoppingCartHttpClient.props)

  def createShoppingCart(shoppingCartId: ShoppingCartRef, customerId: CustomerRef): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(CreateShoppingCart(shoppingCartId, customerId)).mapTo[HttpClientResult[ShoppingCartView]]

  def getShoppingCart(shoppingCartId: ShoppingCartRef): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(GetShoppingCart(shoppingCartId)).mapTo[HttpClientResult[ShoppingCartView]]

  def addItem(shoppingCartId: ShoppingCartRef, productId: ProductRef, count: Int): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(AddItem(shoppingCartId, productId, count)).mapTo[HttpClientResult[ShoppingCartView]]

  def removeItem(shoppingCartId: ShoppingCartRef, productId: ProductRef): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(RemoveItem(shoppingCartId, productId)).mapTo[HttpClientResult[ShoppingCartView]]

  def clearShoppingCart(shoppingCartId: ShoppingCartRef): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(ClearCart(shoppingCartId)).mapTo[HttpClientResult[ShoppingCartView]]
}
