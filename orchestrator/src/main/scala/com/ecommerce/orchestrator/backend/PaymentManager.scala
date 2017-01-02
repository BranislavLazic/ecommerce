package com.ecommerce.orchestrator.backend

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 1/1/17.
  */
object PaymentManager {

  def props = Props(new PaymentManager)

  def name = "payment-manager"

}

class PaymentManager extends Actor{

  def receive = ???
}
