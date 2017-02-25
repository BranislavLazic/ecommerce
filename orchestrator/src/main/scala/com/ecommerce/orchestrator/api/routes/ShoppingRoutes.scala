package com.ecommerce.orchestrator.api.routes

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, _}
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient.HttpClientResult
import com.ecommerce.common.identity.Identity.{ProductRef, CustomerRef, ShoppingCartRef}
import com.ecommerce.common.views.ShoppingCartRequest.AddItemView
import com.ecommerce.common.views.ShoppingCartResponse.ShoppingCartView
import com.ecommerce.orchestrator.backend.RequestViews
import com.ecommerce.orchestrator.backend.orchestrator.ShoppingOrchestrator
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

/**
  * Created by lukewyman on 1/31/17.
  */
trait ShoppingRoutes {
  import CirceSupport._
  import Directives._
  import RequestViews._
  import ShoppingOrchestrator._
  import StatusCodes._
  import akka.pattern.ask
  import io.circe.generic.auto._

  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def shoppingRoutes: Route =
    startShopping ~
    placeInCart ~
    removeFromCart ~
    abandonCart ~
    checkout

  def IdSegment: PathMatcher1[UUID]
  val CustomerId = IdSegment
  val ShoppingCartId = IdSegment
  val ProductId = IdSegment

  def startShopping: Route = {
    post {
      pathPrefix("shop" / "customers" / CustomerId/ "shoppingcarts") { customerId =>
        pathEndOrSingleSlash {
          val orchestrator = system.actorOf(ShoppingOrchestrator.props)
          val ss = StartShopping(ShoppingCartRef(UUID.randomUUID()), CustomerRef(customerId))
          onSuccess(orchestrator.ask(ss).mapTo[HttpClientResult[ShoppingCartView]]) { result =>
            result.fold(complete(BadRequest, _), complete(OK, _))
          }
        }
      }
    }
  }

  def placeInCart: Route = {
    put {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          entity(as[AddItemView]) { aiv =>
            val orchestrator = system.actorOf(ShoppingOrchestrator.props)
            val pic = PlaceInCart(ShoppingCartRef(shoppingCartId), ProductRef(productId), aiv.count, aiv.backorder)
            onSuccess(orchestrator.ask(pic).mapTo[HttpClientResult[ShoppingCartView]]) { result =>
              result.fold(complete(BadRequest, _), complete(OK, _))
            }
          }
        }
      }
    }
  }

  def removeFromCart: Route = {
    delete {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          val orchestrator = system.actorOf(ShoppingOrchestrator.props)
          val rfc = RemoveFromCart(ShoppingCartRef(shoppingCartId), ProductRef(productId))
          onSuccess(orchestrator.ask(rfc).mapTo[HttpClientResult[ShoppingCartView]]) { result =>
            result.fold(complete(BadRequest, _), complete(OK, _))
          }
        }
      }
    }
  }

  def abandonCart: Route = {
    delete {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId ) { shoppingCartId  =>
        pathEndOrSingleSlash {
          val orchestrator = system.actorOf(ShoppingOrchestrator.props)
          val ac = AbandonCart(ShoppingCartRef(shoppingCartId))
          onSuccess(orchestrator.ask(ac).mapTo[HttpClientResult[ShoppingCartView]]) { result =>
            result.fold(complete(BadRequest, _), complete(OK, _))
          }
        }
      }
    }
  }

  // Checkout is returniing a ShoppingCartView for now. Will return an OrderView when the Order microservice is done.
  def checkout: Route = {
    post {
      pathPrefix("shop" / "shoppingcarts" / ShoppingCartId / "payments" ) { shoppingCartId =>
        pathEndOrSingleSlash {
          entity(as[CheckoutView]) { cv =>
            val orchestrator = system.actorOf(ShoppingOrchestrator.props)
            val co = Checkout(ShoppingCartRef(shoppingCartId), cv.creditCard)
            onSuccess(orchestrator.ask(co).mapTo[HttpClientResult[ShoppingCartView]]) { result =>
              result.fold(complete(BadRequest, _), complete(OK, _))
            }
          }
        }
      }
    }
  }
}
