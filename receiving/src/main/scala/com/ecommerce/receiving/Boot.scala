package com.ecommerce.receiving

import akka.actor.ActorSystem
import com.ecommerce.receiving.api.ReceivingServiceSupport
import com.ecommerce.receiving.backend.Shipments

/**
  * Created by lukewyman on 2/3/17.
  */
object Boot extends App with ReceivingServiceSupport {
  implicit val system = ActorSystem("receiving")

  val shipments = system.actorOf(Shipments.props, Shipments.name)

  start(shipments)
}
