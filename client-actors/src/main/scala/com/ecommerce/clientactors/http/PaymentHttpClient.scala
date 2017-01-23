package com.ecommerce.clientactors.http

import akka.actor.{Actor, Props}

/**
  * Created by lukewyman on 1/17/17.
  */
object PaymentHttpClient {

  def props = Props(new PaymentHttpClient)

  def name = "payment-manager"

  case class Pay(creditCard: String)
}

class PaymentHttpClient extends Actor {
  import PaymentHttpClient._

  def receive = {
    case Pay(cc) =>
  }
}
