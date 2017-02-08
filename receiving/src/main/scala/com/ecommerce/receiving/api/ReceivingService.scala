package com.ecommerce.receiving.api

import java.util.UUID

import akka.actor.{ActorSystem, ActorRef}
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport


import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 2/5/17.
  */
class ReceivingService(val shipments: ActorRef, val system: ActorSystem, val receiveTimeout: Timeout) extends ReceivingRoutes {
  val executionContext = system.dispatcher
}

trait ReceivingRoutes {
  import Directives._
  import StatusCodes._
  import CirceSupport._
  import io.circe.generic.auto._
  import akka.pattern.ask

  def shipments: ActorRef

  implicit def receiveTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    receiveShipment ~
    acknowledgeShipment ~
    getShipment ~
    createShipment

  def createShipment: Route = {
    post {
      pathPrefix("shipments") {
        pathEndOrSingleSlash {
          complete(OK)
        }
      }
    }
  }

  def getShipment: Route = {
    get {
      pathPrefix("shipments" / ShippingId) { shipmentId =>
        pathEndOrSingleSlash {
          complete(OK, shipmentId)
        }
      }
    }
  }

  def acknowledgeShipment: Route = {
    post {
      pathPrefix("shipments" / ShippingId / "acknowledgments") { shipmentId =>
        pathEndOrSingleSlash {
          complete(OK, shipmentId)
        }
      }
    }
  }

  def receiveShipment: Route = {
    post {
      pathPrefix("shipments" / ShippingId / "deliveries" ) { shipmentId =>
        pathEndOrSingleSlash {
          complete(OK, shipmentId)
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ShippingId = IdSegment
}
