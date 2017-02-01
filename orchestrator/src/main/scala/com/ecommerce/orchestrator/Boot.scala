package com.ecommerce.orchestrator

import akka.actor.ActorSystem
import com.ecommerce.orchestrator.api.OrchestratorServiceSupport

/**
  * Created by lukewyman on 1/1/17.
  */
object Boot extends App with OrchestratorServiceSupport {
  implicit val system = ActorSystem("orchestrator")

  start
}
