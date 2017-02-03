package com.ecommerce.orchestrator.api

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server._
import akka.util.Timeout
import com.ecommerce.clientactors.http.HttpClient.HttpClientResult
import com.ecommerce.clientactors.http.RequestViews.AddItemView
import com.ecommerce.clientactors.http.ResponseViews.ShoppingCartView
import com.ecommerce.orchestrator.api.RequestViews.CheckoutView
import com.ecommerce.orchestrator.backend.actor.orchestrator.ShoppingOrchestrator
import com.ecommerce.orchestrator.backend.actor.orchestrator.ShoppingOrchestrator._
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 1/31/17.
  */
trait ShoppingRoutes {

  import akka.pattern.ask
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
        pathEndOrSingleSlash {
          val orchestrator = system.actorOf(ShoppingOrchestrator.props, ShoppingOrchestrator.name)
          val ss = StartShopping(UUID.randomUUID(), customerId)
          onSuccess(orchestrator.ask(ss).mapTo[HttpClientResult[ShoppingCartView]]) { result =>
            val test: Either[String, String] = Right("test")
            complete(test)
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
            val orchestrator = system.actorOf(ShoppingOrchestrator.props, ShoppingOrchestrator.name)
            val pic = PlaceInCart(shoppingCartId, productId, aiv.count, aiv.backorder)
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
          val orchestrator = system.actorOf(ShoppingOrchestrator.props, ShoppingOrchestrator.name)
          val rfc = RemoveFromCart(shoppingCartId, productId)
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
          val orchestrator = system.actorOf(ShoppingOrchestrator.props, ShoppingOrchestrator.name)
          val ac = AbandonCart(shoppingCartId)
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
            val orchestrator = system.actorOf(ShoppingOrchestrator.props, ShoppingOrchestrator.name)
            val co = Checkout(shoppingCartId, cv.creditCard)
            onSuccess(orchestrator.ask(co).mapTo[HttpClientResult[ShoppingCartView]]) { result =>
              result.fold(complete(BadRequest, _), complete(OK, _))
            }
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
