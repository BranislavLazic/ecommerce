package com.ecommerce.orchestrator.backend.orchestrator

import java.util.UUID

import akka.actor.{ActorLogging, Actor, Props}
import akka.util.Timeout
import cats.data.EitherT
import cats.implicits._
import com.ecommerce.common.clientactors.http._
import com.ecommerce.common.clientactors.kafka._
import com.ecommerce.orchestrator.backend.clientapi.{ShoppingCartApi, PaymentApi, InventoryApi}

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
}

class ShoppingOrchestrator extends Actor with ActorLogging
  with ShoppingCartApi
  with InventoryApi
  with PaymentApi {
  import ShoppingOrchestrator._
  import akka.pattern.pipe

  implicit def executionContext = context.dispatcher
  implicit def timeout: Timeout = Timeout(3 seconds)

  def inventoryClient = context.actorOf(InventoryHttpClient.props, InventoryHttpClient.name)
  def inventoryQueue = context.actorOf(InventoryKafkaClient.props, InventoryKafkaClient.name)
  def paymentClient = context.actorOf(PaymentHttpClient.props, PaymentHttpClient.name)
  def shoppingCartClient = context.actorOf(ShoppingCartHttpClient.props, ShoppingCartHttpClient.name)

  def receive = {
    case StartShopping(scid, cid) =>
      createShoppingCart(scid, cid).pipeTo(sender())
      kill()
    case PlaceInCart(scid, iid, c, bo) =>
      val pic = EitherT(holdInventory(scid, iid, c))
        .flatMapF(iv => addItem(scid, iid, iv.count))
      pic.value.pipeTo(sender())
      kill()
    case RemoveFromCart(scid, iid) =>
      val rfc = removeItem(scid, iid)
      releaseInventory(scid, iid)
      rfc.pipeTo(sender())
      kill()
    case AbandonCart(scid) =>
      val scf = EitherT(getShoppingCart(scid))
      scf.map(sc => sc.items.foreach(i => releaseInventory(scid, i.itemId)))
      scf.flatMapF(sc => clearShoppingCart(sc.shoppingCartId)).value.pipeTo(sender())
      kill()
    case Checkout(scid, cc) =>
      /* what should happen here is that an Order should be created, and the Order with it's OrderStatus
        should be returned to the caller. Returning the ShoppingCartView is a stop-gap until the Order
        microservice is built out.And, yes, I know that payment/amount due is woefully over-simplified here */
      val result = for {
        sc <- EitherT(getShoppingCart(scid))
        p <- EitherT(pay(cc))
        cs = sc.items.map(i => claimInventory(sc.shoppingCartId, i.itemId, p.paymentId))
      } yield sc
      result.value.pipeTo(sender())
      kill()
  }

  def kill() = log.info("killing children and self after processing message") // TODO: implementation to kill http cleint actors and self
}