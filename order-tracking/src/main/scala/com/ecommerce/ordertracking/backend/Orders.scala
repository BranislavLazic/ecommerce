package com.ecommerce.ordertracking.backend

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 2/4/17.
  */
object Orders {

  val props = Props(new Orders)

  val name = "orders"
}

class Orders extends Actor {

  def receive = ???
}
