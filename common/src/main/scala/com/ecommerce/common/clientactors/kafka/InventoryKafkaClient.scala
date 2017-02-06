package com.ecommerce.common.clientactors.kafka

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 2/5/17.
  */
object InventoryKafkaClient {

  val props = Props(new InventoryKafkaClient)

  val name = "inventory-kafka-client"
}

class InventoryKafkaClient extends Actor {

  def receive = ???

}