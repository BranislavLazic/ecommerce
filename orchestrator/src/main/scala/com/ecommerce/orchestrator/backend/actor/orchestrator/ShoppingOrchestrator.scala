package com.ecommerce.orchestrator.backend.actor.orchestrator

import java.util.UUID

import akka.actor.{ActorRef, Actor, Props}
import akka.util.Timeout
import cats.data.EitherT
import cats.implicits._
import com.ecommerce.clientactors.http.PaymentHttpClient.Pay
import com.ecommerce.clientactors.http.RequestViews.{ClaimItemView, HoldItemView, AddItemView}
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
  case class Checkout(shoppingCartId: UUID, creditCard: String)
  case class PlaceInCart(shoppingCartId: UUID, itemId: UUID, count: Int, backorder: Boolean)
  case class RemoveFromCart(shoppingCartId: UUID, itemId: UUID)
  case class ClearCart(shoppingCartId: UUID)
}

class ShoppingOrchestrator extends Actor with ShoppingOrchestratorApi {
  import akka.pattern.pipe
  import ShoppingOrchestrator._

  implicit def executionContext = context.dispatcher

  def inventoryClient = context.actorOf(InventoryHttpClient.props, InventoryHttpClient.name)
  def inventoryQueue = context.actorOf(InventoryKafkaClient.props, InventoryKafkaClient.name)
  def paymentClient = context.actorOf(PaymentHttpClient.props, PaymentHttpClient.name)
  def shoppingCartClient = context.actorOf(ShoppingCartHttpClient.props, ShoppingCartHttpClient.name)

  def receive = {
    case StartShopping(scid, cid) =>
      createShoppingCart(scid, cid).pipeTo(sender())
    case PlaceInCart(scid, iid, c, bo) =>
      val pic = EitherT(holdInventory(scid, iid, c))
        .flatMapF(iv => addItem(scid, iid, iv.count))
      pic.value.pipeTo(sender())
    case RemoveFromCart(scid, iid) =>
      val rfc = removeItem(scid, iid)
      releaseInventory(scid, iid)
      rfc.pipeTo(sender())
    case AbandonCart(scid) =>
      val scf = EitherT(getShoppingCart(scid))
      scf.map(sc => sc.items.foreach(i => releaseInventory(scid, i.itemId)))
      scf.flatMapF(sc => clearShoppingCart(sc.shoppingCartId)).value.pipeTo(sender())
    case Checkout(scid, cc) =>
      /* what should happen here is that an Order should be created, and the Order with it's OrderStatus
        should be returned to the caller. Returning the ShoppingCartView is a stop-gap until the Order
        microservice is built out.And, yes, I know that payment/amount due is woefully over-simplified here */
      val scF = EitherT(getShoppingCart(scid))
      val pF = EitherT(pay(cc))
      val result = for {
        sc <- scF
        p <- pF
        cs = sc.items.map(i => claimInventory(sc.shoppingCartId, i.itemId, p.paymentId))
      } yield sc
      result.value.pipeTo(sender())
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
  def inventoryQueue: ActorRef
  def paymentClient: ActorRef
  def shoppingCartClient: ActorRef

  def createShoppingCart(shoppingCartId: UUID, customerId: UUID) =
    shoppingCartClient.ask(CreateShoppingCart(shoppingCartId, customerId)).mapTo[HttpClientResult[ShoppingCartView]]

  def getShoppingCart(shoppingCartId: UUID): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(GetShoppingCart(shoppingCartId)).mapTo[HttpClientResult[ShoppingCartView]]

  def addItem(shoppingCartId: UUID, itemId: UUID, count: Int): Future[HttpClientResult[AddItemView]] =
    shoppingCartClient.ask(AddItem(shoppingCartId, itemId, count)).mapTo[HttpClientResult[AddItemView]]

  def removeItem(shoppingCartId: UUID, itemId: UUID): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(RemoveItem(shoppingCartId, itemId)).mapTo[HttpClientResult[ShoppingCartView]]

  def clearShoppingCart(shoppingCartId: UUID): Future[HttpClientResult[ShoppingCartView]] =
    shoppingCartClient.ask(ClearCart(shoppingCartId)).mapTo[HttpClientResult[ShoppingCartView]]

  def releaseInventory(shoppingCartId: UUID, itemId: UUID) =
    inventoryQueue ! ReleaseItem(itemId, shoppingCartId)

  def holdInventory(shoppingCartId: UUID, itemId: UUID, count: Int): Future[HttpClientResult[HoldItemView]] =
    inventoryClient.ask(HoldItem(shoppingCartId, itemId, count)).mapTo[HttpClientResult[HoldItemView]]

  def claimInventory(shoppingCartId: UUID, itemId: UUID, paymentId: UUID): Future[HttpClientResult[ClaimItemView]] =
    inventoryClient.ask(ClaimItem(shoppingCartId, itemId, paymentId)).mapTo[HttpClientResult[ClaimItemView]]

  def pay(creditCard: String): Future[HttpClientResult[PaymentTokenView]] =
    paymentClient.ask(Pay(creditCard)).mapTo[HttpClientResult[PaymentTokenView]]
}