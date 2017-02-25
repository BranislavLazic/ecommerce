package com.ecommerce.common.clientactors.http

import akka.actor.{Actor, Props}

/**
  * Created by lukewyman on 2/5/17.
  */
object PaymentHttpClient {

  def props = Props(new PaymentHttpClient)

  case class Pay(creditCard: String)
}

class PaymentHttpClient extends Actor {
  import PaymentHttpClient._

  def receive = {
    case Pay(cc) =>
  }
}
