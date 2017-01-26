package com.ecommerce.shoppingcart.api

import java.util.UUID
import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.model.StatusCodes._
import com.ecommerce.shoppingcart.backend.ShoppingCart.{ItemRef, CustomerRef, ShoppingCartRef}
import com.ecommerce.shoppingcart.backend.{SetOwner, AddItem, RemoveItem, GetItems, GetItemsResult}
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 12/11/16.
  */
class ShoppingCartService(val shoppingCarts: ActorRef, val system: ActorSystem, val requestTimeout: Timeout) extends ShoppingCartRoutes {
  val executionContext = system.dispatcher
}

trait ShoppingCartRoutes {

  import CirceSupport._
  import Directives._
  import io.circe.generic.auto._
  import RequestViews._
  import ResponseMappers._

  def shoppingCarts: ActorRef

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    updateItem ~
    getShoppingCart ~
    createShoppingCart

  def createShoppingCart: Route = {
    post {
      pathPrefix("shoppingcarts") {
        pathEndOrSingleSlash {
          entity(as[CreateShoppingCartView]) { cscv =>
            val setOwner = SetOwner(ShoppingCartRef(cscv.shoppingCartId), CustomerRef(cscv.customerId))
            shoppingCarts ! setOwner
            complete(OK)
          }
        }
      }
    }
  }

  def updateItem: Route = {
    put {
      pathPrefix("shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          entity(as[AddItemCountView]) { aic =>
            val addItem = AddItem(ShoppingCartRef(shoppingCartId), ItemRef(productId), aic.count)
            shoppingCarts ! addItem
            complete(OK)
          }
        }
      }
    }
  }

  def removeItem: Route = {
    delete {
      pathPrefix("shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          val removeItem = RemoveItem(ShoppingCartRef(shoppingCartId), ItemRef(productId))
          shoppingCarts ! removeItem
          complete(OK)
        }
      }
    }
  }

  def getShoppingCart: Route = {
    get {
      pathPrefix("shoppingcarts" / ShoppingCartId) { shoppingCartId =>
        pathEndOrSingleSlash {
          val getItems = GetItems(ShoppingCartRef(shoppingCartId))
          onSuccess(shoppingCarts.ask(getItems).mapTo[GetItemsResult]) {
            case result => complete(mapToShoppingCartView(result))
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ProductId = IdSegment
  val ShoppingCartId = IdSegment
}

