package com.ecommerce.ordertracking

import akka.actor.ActorSystem
import com.ecommerce.ordertracking.api.OrdersServiceSupport
import com.ecommerce.ordertracking.backend.Orders

/**
  * Created by lukewyman on 2/3/17.
  */
object Boot extends App with OrdersServiceSupport {
  implicit val system = ActorSystem("orders")

  val orders = system.actorOf(Orders.props, Orders.name)

  start(orders)
}
