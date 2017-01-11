package com.ecommerce.orchestrator.backend.actor.httpclient

import akka.actor.{Actor, Props}

/**
  * Created by lukewyman on 1/1/17.
  */
object PaymentClient {

  def props = Props(new PaymentClient)

  def name = "payment-manager"

  case class Pay(creditCard: String)
}

class PaymentClient extends Actor{
  import PaymentClient._

  def receive = {
    case Pay(cc) =>
  }
}
