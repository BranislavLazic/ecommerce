package com.ecommerce.orchestrator.api.routes

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, _}
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient.HttpClientResult
import com.ecommerce.common.identity.Identity.{ProductRef, ShipmentRef}
import com.ecommerce.orchestrator.backend.orchestrator.ReceivingOrchestrator
import com.ecommerce.orchestrator.backend.{RequestViews, ResponseViews}
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext

/**
  * Created by lukewyman on 2/8/17.
  */
trait ReceivingRoutes {
  import Directives._
  import CirceSupport._
  import io.circe.generic.auto._
  import io.circe.java8.time._
  import ReceivingOrchestrator._
  import RequestViews._
  import ResponseViews._
  import StatusCodes._
  import akka.pattern.ask

  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def receivingRoutes: Route =
    getShipment ~
    requestShipment ~
    acknowledgeShipment ~
    acceptShipment

  def IdSegment: PathMatcher1[UUID]
  val ShipmentId = IdSegment


  def getShipment: Route = {
    get {
      pathPrefix( "receiving" / "shipments" / ShipmentId ) { shipmentId =>
        pathEndOrSingleSlash {
          val gs = GetShipmentSummary(ShipmentRef(shipmentId))
          val receivingOrchestrator = system.actorOf(ReceivingOrchestrator.props)
          onSuccess(receivingOrchestrator.ask(gs).mapTo[HttpClientResult[ReceivingSummaryView]]) {
            _.fold(complete(BadRequest, _), complete(OK, _))
          }
        }
      }
    }
  }

  def requestShipment: Route = {
    post {
      pathPrefix( "receiving" / "shipments" ) {
        pathEndOrSingleSlash {
          entity(as[RequestShipmentView]) { rsv =>
            val rs = RequestShipment(ProductRef(rsv.productId), rsv.ordered, rsv.count)
            val receivingOrchestrator = system.actorOf(ReceivingOrchestrator.props)
            onSuccess(receivingOrchestrator.ask(rs).mapTo[HttpClientResult[ReceivingSummaryView]]) {
              _.fold(complete(BadRequest, _), complete(OK, _))
            }
          }
        }
      }
    }
  }

  def acknowledgeShipment: Route = {
    post {
      pathPrefix( "receiving" / "shipments" / ShipmentId / "acknowledgments" ) { shipmentId =>
        pathEndOrSingleSlash {
          entity(as[AcknowledgeShipmentView]) { asv =>
            val as = AcknowledgeShipment(ProductRef(asv.productId), ShipmentRef(shipmentId), asv.expectedDelivery, asv.count)
            val receivingOrchestrator = system.actorOf(ReceivingOrchestrator.props)
            onSuccess(receivingOrchestrator.ask(as).mapTo[HttpClientResult[ReceivingSummaryView]]) {
              _.fold(complete(BadRequest, _), complete(OK, _))
            }
          }
        }
      }
    }
  }

  def acceptShipment: Route = {
    post {
      pathPrefix( "receiving" / "shipments" / ShipmentId / "deliveries" ) { shipmentId =>
        pathEndOrSingleSlash {
          entity(as[AcceptShipmentView]) { asv =>
            val as = AcceptShipment(ProductRef(asv.productId), ShipmentRef(shipmentId), asv.delivered, asv.count)
            val receivingOrchestrator = system.actorOf(ReceivingOrchestrator.props)
            onSuccess(receivingOrchestrator.ask(as).mapTo[HttpClientResult[ReceivingSummaryView]]) {
              _.fold(complete(BadRequest, _), complete(OK, _))
            }
          }
        }
      }
    }
  }

}
