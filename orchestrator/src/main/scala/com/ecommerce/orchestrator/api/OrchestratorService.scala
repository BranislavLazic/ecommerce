package com.ecommerce.orchestrator.api

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport
import com.ecommerce.orchestrator.api.RequestViews.CheckoutView
import com.ecommerce.orchestrator.backend.actor.orchestrator.ShoppingOrchestrator

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
  import CirceSupport._
  import io.circe.generic.auto._


  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  val routes: Route = ???

  def checkout: Route = {
    post {
      pathPrefix("shoppingcarts" / ShoppingCartId / "checkout") { shoppingCartId =>
        pathEndOrSingleSlash {
          entity(as[CheckoutView]) { cv =>
            def checkoutOrchestrator = system.actorOf(ShoppingOrchestrator.props, ShoppingOrchestrator.name)
            complete(OK)
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ShoppingCartId = IdSegment
}
