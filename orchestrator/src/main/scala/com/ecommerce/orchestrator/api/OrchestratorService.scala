package com.ecommerce.orchestrator.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.ecommerce.orchestrator.api.RequestViews.CheckoutView
import com.ecommerce.orchestrator.backend.CheckoutOrchestrator

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 1/1/17.
  */
case class OrchestratorService(val system: ActorSystem, val requestTimeout: Timeout) extends OrchestratorRoutes {
  val executionContext = system.dispatcher
}

trait OrchestratorRoutes {

  import Directives._

  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  val routes: Route = ???

  def checkout: Route = {
    post {
      pathPrefix("shoppingcarts" / ShoppingCartId / "checkout") { shoppingCartId =>
        pathEndOrSingleSlash {
          entity(as[CheckoutView]) { cv =>
            def checkoutOrchestrator = system.actorOf(CheckoutOrchestrator.props, CheckoutOrchestrator.name)
            checkoutOrchestrator ! null
            complete(OK)
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ShoppingCartId = IdSegment
}
