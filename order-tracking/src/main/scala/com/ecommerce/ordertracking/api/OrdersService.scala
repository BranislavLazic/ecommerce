package com.ecommerce.ordertracking.api

import java.util.UUID

import akka.actor.{ActorSystem, ActorRef}
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import com.ecommerce.ordertracking.api.RequestViews.{UpdateOrderItemView, CreateOrderView}
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 2/4/17.
  */
class OrdersService(val orders: ActorRef, val system: ActorSystem, val requestTimeout: Timeout) extends OrdersRoutes {
  val executionContext = system.dispatcher
}

trait OrdersRoutes {
  import Directives._
  import StatusCodes._
  import CirceSupport._
  import io.circe.generic.auto._

  def orders: ActorRef

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    updateOrderItem ~
    getOrder ~
    createOrder

  def createOrder: Route = {
    post {
      pathPrefix("orders") {
        pathEndOrSingleSlash {
          entity(as[CreateOrderView]) { cov =>
            complete(OK)
          }
        }
      }
    }
  }

  def getOrder: Route = {
    get {
      pathPrefix("orders" / OrderId ) { orderId =>
        pathEndOrSingleSlash {
          complete(OK, orderId)
        }
      }
    }
  }

  def updateOrderItem: Route = {
    put {
      pathPrefix("orders" / OrderId / "orderritems" / OrderItemId ) { (orderID, orderItemId ) =>
        pathEndOrSingleSlash {
          entity(as[UpdateOrderItemView]) { uoiv =>
            complete(OK)
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val OrderId = IdSegment
  val OrderItemId = IdSegment
}
