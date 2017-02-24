package com.ecommerce.common.clientactors.http

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductHttpClient {

  def props = Props(new ProductHttpClient)

  def name = "product-client"
}

class ProductHttpClient extends Actor {

  def receive = ???
}
