package com.ecommerce.orchestrator.backend.actor.orchestrator

import java.util.UUID

import akka.actor.{ActorRef, Actor, Props}
import akka.util.Timeout
import cats.data.EitherT
import cats.implicits._
import com.ecommerce.clientactors.http.RequestViews.{HoldItemView, AddItemView}
import com.ecommerce.clientactors.http._
import com.ecommerce.clientactors.kafka._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

/**
  * Created by lukewyman on 1/1/17.
  */
object ShoppingOrchestrator {

  def props = Props(new ShoppingOrchestrator)

  def name = "checkout-orchestrator"

  case class StartShopping(shoppingCartId: UUID, customerId: UUID)
  case class AbandonCart(shoppingCartId: UUID)
  case class Checkout(shoppingCartId: UUID)
  case class BuyItem(shoppingCartId: UUID, itemId: UUID, count: Int)
  case class ReserveItem(shoppingCartId: UUID, customerId: UUID, itemId: UUID, count: Int)
}

class ShoppingOrchestrator extends Actor with ShoppingOrchestratorApi {
  import akka.pattern.pipe
  import ShoppingOrchestrator._

  implicit def executionContext = context.dispatcher

  def inventoryClient = context.actorOf(InventoryHttpClient.props, InventoryHttpClient.name)
  def paymentClient = context.actorOf(PaymentHttpClient.props, PaymentHttpClient.name)
  def shoppingCartClient = context.actorOf(ShoppingCartHttpClient.props, ShoppingCartHttpClient.name)

  def receive = {
    case BuyItem(scid, iid, c) =>
      val bf = EitherT(holdInventory(scid, iid, c))
        .flatMapF(iv => addItem(iv.shoppingCartId, iv.itemId, iv.count))
      bf.value.pipeTo(sender())
    case ReserveItem(scid, cid, iid, c) =>
      val rf = EitherT(null)
    case AbandonCart(scid) =>
      val scf = EitherT(getShoppingCart(scid))
      scf.map(sc => sc.items.foreach(i => releaseInventory(scid, i.itemId)))
      scf.flatMapF(sc => clearShoppingCart(sc.shoppingCartId)).value.pipeTo(sender())
    case Checkout(scid) =>

  }
}

trait ShoppingOrchestratorApi { this: Actor =>
  import akka.pattern.ask
  import HttpClient._
  import InventoryKafkaClient._
  import ShoppingCartHttpClient._
  import ResponseViews._

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout = Timeout(3 seconds)

  def inventoryClient: ActorRef
  def paymentClient: ActorRef
  def shoppingCartClient: ActorRef

  def getShoppingCart(shoppingCartId: UUID): Future[Either[HttpClientError, ShoppingCartView]] =
    shoppingCartClient.ask(GetShoppingCart(shoppingCartId)).mapTo[Either[HttpClientError, ShoppingCartView]]

  def addItem(shoppingCartId: UUID, itemId: UUID, count: Int): Future[HttpClientResult[AddItemView]] =
    shoppingCartClient.ask(AddItem(shoppingCartId, itemId, count)).mapTo[HttpClientResult[AddItemView]]

  def clearShoppingCart(shoppingCartId: UUID): Future[Either[HttpClientError, ShoppingCartView]] =
    shoppingCartClient.ask(ClearCart(shoppingCartId)).mapTo[HttpClientResult[ShoppingCartView]]

  def releaseInventory(shoppingCartId: UUID, itemId: UUID) = inventoryClient ! ReleaseItem(itemId, shoppingCartId)

  def holdInventory(shoppingCartId: UUID, itemId: UUID, count: Int): Future[HttpClientResult[HoldItemView]] = ???

  def reseerveInventory(shoppingCartId: UUID, customerId: UUID, itemId: UUID, count: Int): Future[HttpClientResult[]] = ???

  def makePayment()
}