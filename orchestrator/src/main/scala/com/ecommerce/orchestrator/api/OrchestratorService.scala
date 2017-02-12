package com.ecommerce.orchestrator.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.{Route, Directives}
import akka.util.Timeout
import com.ecommerce.orchestrator.api.routes.{ShoppingRoutes, ReceivingRoutes}

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 1/1/17.
  */
case class OrchestratorService(val system: ActorSystem, val requestTimeout: Timeout) extends OrchestratorRoutes {
  val executionContext = system.dispatcher
}

trait OrchestratorRoutes extends ReceivingRoutes with ShoppingRoutes {
  import Directives._

  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    receivingRoutes ~
    shoppingRoutes

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
}
