package com.ecommerce.orchestrator.backend.actor.orchestrator

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 2/6/17.
  */
object ReceivingOrchestrator {

  val props = Props(new ReceivingOrchestrator)

  val name = "receiving-orchestrator"

}

class ReceivingOrchestrator extends Actor {

  def receive = ???
}


