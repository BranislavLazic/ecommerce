package com.ecommerce.orchestrator.api

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server._
import akka.util.Timeout
import com.ecommerce.orchestrator.api.RequestViews.CheckoutView
import com.ecommerce.orchestrator.backend.actor.orchestrator.ShoppingOrchestrator
import com.ecommerce.orchestrator.backend.actor.orchestrator.ShoppingOrchestrator.StartShopping
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 1/31/17.
  */
trait ShoppingRoutes {

  import Directives._
  import StatusCodes._
  import CirceSupport._
  import io.circe.generic.auto._

  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def shoppingRoutes: Route = ???

  def startShopping: Route = {
    post {
      pathPrefix("shop" / "customers" / CustomerId/ "shoppingcarts") { customerId =>
        val orchestrator = system.actorOf(ShoppingOrchestrator.props, ShoppingOrchestrator.name)
        val ss = StartShopping(UUID.randomUUID(), customerId)
        orchestrator ! ss
        complete(OK)
      }
    }
  }

  def addItem: Route = {
    put {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          complete(OK)
        }
      }
    }
  }

  def removeItem: Route = {
    delete {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          complete(OK)
        }
      }
    }
  }

  def abandonCart: Route = {
    delete {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId ) { shoppingCartId  =>
        complete(OK)
      }
    }
  }

  def checkout: Route = {
    post {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId / "payments" ) { shoppingCartId =>
        pathEndOrSingleSlash {
          entity(as[CheckoutView]) { cv =>
            complete(OK)
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val CustomerId = IdSegment
  val ShoppingCartId = IdSegment
  val ProductId = IdSegment
}
