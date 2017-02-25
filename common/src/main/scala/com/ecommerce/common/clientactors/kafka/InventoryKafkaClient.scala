package com.ecommerce.common.clientactors.kafka

import akka.actor.{ActorLogging, Props, Actor}

/**
  * Created by lukewyman on 2/5/17.
  */
object InventoryKafkaClient {

  val props = Props(new InventoryKafkaClient)
}

class InventoryKafkaClient extends Actor with ActorLogging {

  def receive = {
    case _ => log.info("received something")
  }

}