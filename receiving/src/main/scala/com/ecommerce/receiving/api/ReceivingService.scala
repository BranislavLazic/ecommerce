package com.ecommerce.receiving.api

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ActorSystem, ActorRef}
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import com.ecommerce.receiving.backend._
import com.ecommerce.receiving.backend.Shipment.{ProductRef, ShipmentRef}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import com.ecommerce.common.views.ReceivingRequest
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
  import io.circe.java8.time._
  import akka.pattern.ask
  import ReceivingRequest._
  import ResponseMappers._

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
          entity(as[CreateShipmentView]) { csv =>
            val setProductAndCount = SetProductAndCount(ShipmentRef(UUID.randomUUID()), ProductRef(csv.productId), csv.count)
            onSuccess(shipments.ask(setProductAndCount).mapTo[ManagerResponse]) {
              case GetShipmentResult(sid, s) => complete(OK, mapToShipmentView(sid, s))
              case Rejection(r) => complete(BadRequest, r)
            }
          }
        }
      }
    }
  }

  def getShipment: Route = {
    get {
      pathPrefix("shipments" / ShippingId) { shipmentId =>
        pathEndOrSingleSlash {
          val getShipment = GetShipment(ShipmentRef(shipmentId))
          onSuccess(shipments.ask(getShipment).mapTo[ManagerResponse]) {
            case GetShipmentResult(sid, s) => complete(OK, mapToShipmentView(sid, s))
            case Rejection(r) => complete(BadRequest, r)
          }
        }
      }
    }
  }

  def acknowledgeShipment: Route = {
    post {
      pathPrefix("shipments" / ShippingId / "acknowledgments") { shipmentId =>
        pathEndOrSingleSlash {
          entity(as[AcknowledgeShipmentView]) { asv =>
            val ackShip = UpdateExpectedDelivery(ShipmentRef(shipmentId), asv.expectedDelivery)
            onSuccess(shipments.ask(ackShip).mapTo[ManagerResponse]) {
              case GetShipmentResult(sid, s) => complete(OK, mapToShipmentView(sid, s))
              case Rejection(r) => complete(BadRequest, r)
            }
          }
        }
      }
    }
  }

  def receiveShipment: Route = {
    post {
      pathPrefix("shipments" / ShippingId / "deliveries" ) { shipmentId =>
        pathEndOrSingleSlash {
          val accShip = ReceiveDelivery(ShipmentRef(shipmentId), ZonedDateTime.now)
          onSuccess(shipments.ask(accShip).mapTo[ManagerResponse]) {
            case GetShipmentResult(sid, s) => complete(OK, mapToShipmentView(sid, s))
            case Rejection(r) => complete(BadRequest, r)
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ShippingId = IdSegment
}
