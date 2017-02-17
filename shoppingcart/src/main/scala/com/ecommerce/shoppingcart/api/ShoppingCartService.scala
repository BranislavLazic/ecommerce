package com.ecommerce.shoppingcart.api

import java.util.UUID
import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.model.StatusCodes
import com.ecommerce.common.identity.Identity
import com.ecommerce.shoppingcart.backend._
import com.ecommerce.common.views.ShoppingCartRequest
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
  import StatusCodes._
  import io.circe.generic.auto._
  import ShoppingCartRequest._
  import ResponseMappers._
  import Identity._

  def shoppingCarts: ActorRef

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    addItem ~
    getShoppingCart ~
    createShoppingCart

  def createShoppingCart: Route = {
    post {
      pathPrefix("shoppingcarts") {
        pathEndOrSingleSlash {
          entity(as[CreateShoppingCartView]) { cscv =>
            val setOwner = SetOwner(ShoppingCartRef(cscv.shoppingCartId), CustomerRef(cscv.customerId))
            getRoute(setOwner)
          }
        }
      }
    }
  }

  def addItem: Route = {
    put {
      pathPrefix("shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          entity(as[AddItemView]) { aiv =>
            val addItem = AddItem(ShoppingCartRef(shoppingCartId), ProductRef(productId), aiv.count)
            getRoute(addItem)
          }
        }
      }
    }
  }

  def removeItem: Route = {
    delete {
      pathPrefix("shoppingcarts" / ShoppingCartId / "items" / ProductId) { (shoppingCartId, productId) =>
        pathEndOrSingleSlash {
          val removeItem = RemoveItem(ShoppingCartRef(shoppingCartId), ProductRef(productId))
          getRoute(removeItem)
        }
      }
    }
  }

  def getShoppingCart: Route = {
    get {
      pathPrefix("shoppingcarts" / ShoppingCartId) { shoppingCartId =>
        pathEndOrSingleSlash {
          val getItems = GetItems(ShoppingCartRef(shoppingCartId))
          getRoute(getItems)
        }
      }
    }
  }

  def getRoute(msg: ShoppingCartMessage) =
    onSuccess(shoppingCarts.ask(msg).mapTo[ManagerResponse]) {
      case gscr: GetShoppingCartResult => complete(OK, mapToShoppingCartView(gscr.shoppingCartId, gscr.shoppingCart))
      case Rejection(reason) => complete(BadRequest, reason)
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ProductId = IdSegment
  val ShoppingCartId = IdSegment
}

