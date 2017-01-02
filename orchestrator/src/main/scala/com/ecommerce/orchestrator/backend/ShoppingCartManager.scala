package com.ecommerce.orchestrator.backend

import akka.actor.{Actor, Props}

/**
  * Created by lukewyman on 1/1/17.
  */
object ShoppingCartManager {

  def props = Props(new ShoppingCartManager)

  def name = "shoppingcart-manager"
}

class ShoppingCartManager extends Actor {

  def receive = ???
}
