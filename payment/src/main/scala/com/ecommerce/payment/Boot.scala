package com.ecommerce.payment

import akka.actor.ActorSystem
import com.ecommerce.payment.api.PaymentServiceSupport

/**
  * Created by lukewyman on 2/3/17.
  */
object Boot extends App with PaymentServiceSupport {
  implicit val system = ActorSystem("payment")

  start
}
