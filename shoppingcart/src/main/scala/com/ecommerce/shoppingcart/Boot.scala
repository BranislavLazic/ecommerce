package com.ecommerce.shoppingcart

import akka.actor.ActorSystem
import com.ecommerce.shoppingcart.api.ShoppingCartServiceSupport
import com.ecommerce.shoppingcart.backend.ShoppingCarts

/**
  * Created by lukewyman on 12/11/16.
  */
object Boot extends App with ShoppingCartServiceSupport {
  implicit val system = ActorSystem("shoppingcarts")

  val shoppingCarts = system.actorOf(ShoppingCarts.props, ShoppingCarts.name)

  start(shoppingCarts)
}
