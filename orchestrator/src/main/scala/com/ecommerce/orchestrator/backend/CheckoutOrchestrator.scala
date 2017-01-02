package com.ecommerce.orchestrator.backend

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 1/1/17.
  */
object CheckoutOrchestrator {

  def props = Props(new CheckoutOrchestrator)

  def name = "checkout-orchestrator"

}

class CheckoutOrchestrator extends Actor {

  def paymentManager = context.actorOf(PaymentManager.props, PaymentManager.name)
  def shoppingcartManager = context.actorOf(ShoppingCartManager.props, ShoppingCartManager.name)

  def receive = ???
}
