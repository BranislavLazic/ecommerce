package com.ecommerce.orchestrator.backend.actor.orchestrator

import java.util.UUID

import akka.actor.{Actor, Props}
import com.ecommerce.clientactors.http.{InventoryClient, ShoppingCartClient, PaymentClient}

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

class ShoppingOrchestrator extends Actor {
  import ShoppingOrchestrator._
  import ShoppingCartClient._
  import InventoryClient._

  def paymentClient = context.actorOf(PaymentClient.props, PaymentClient.name)
  def shoppingCartClient = context.actorOf(ShoppingCartClient.props, ShoppingCartClient.name)
  def inventoryClient = context.actorOf(InventoryClient.props, InventoryClient.name)

  def receive = ???
}