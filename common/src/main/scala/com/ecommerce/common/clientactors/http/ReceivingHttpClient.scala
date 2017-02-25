package com.ecommerce.common.clientactors.http

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{Props, Actor}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import com.ecommerce.common.identity.Identity
import de.heikoseeberger.akkahttpcirce.CirceSupport
import com.ecommerce.common.clientactors.protocols.ReceivingProtocol
import com.ecommerce.common.views.ReceivingRequest
import com.ecommerce.common.views.ReceivingResponse

import scala.concurrent.Future

/**
  * Created by lukewyman on 2/6/17.
  */
object ReceivingHttpClient {

  val props = Props(new ReceivingHttpClient)
}

class ReceivingHttpClient extends Actor with ReceivingHttpClientApi {
  import ReceivingProtocol._
  import ReceivingRequest._
  import akka.pattern.pipe

  implicit def executionContext = context.dispatcher
  implicit def system = context.system

  def receive = {
    case CreateShipment(pid, o, c) =>
      createShipment(pid, o, c).pipeTo(sender())
    case GetShipment(sid) =>
      getShipment(sid).pipeTo(sender())
    case AcknowledgeShipment(sid, ed) =>
      acknowledgeShipment(sid, ed).pipeTo(sender())
    case AcceptShipment(sid) =>
      acceptShipment(sid).pipeTo(sender())
  }
}

trait ReceivingHttpClientApi extends HttpClient {

  import CirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.java8.time._
  import ReceivingRequest._
  import ReceivingResponse._
  import HttpClient._
  import Identity._

  def getShipment(shipmentId: ShipmentRef): Future[HttpClientResult[ShipmentView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.GET,
      uri = Uri(path = Path(s"/shipments/${shipmentId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ShipmentView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def createShipment(productId: ProductRef, ordered: ZonedDateTime, count: Int): Future[HttpClientResult[ShipmentView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, CreateShipmentView(productId.id, ordered, count).asJson.toString()),
      uri = Uri(path = Path("/shipments"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ShipmentView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def acknowledgeShipment(shipmentId: ShipmentRef, expectedDelivery: ZonedDateTime): Future[HttpClientResult[ShipmentView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, AcknowledgeShipmentView(expectedDelivery).asJson.toString()),
      uri = Uri(path = Path(s"/shipments/${shipmentId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ShipmentView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def acceptShipment(shipmentId: ShipmentRef): Future[HttpClientResult[ShipmentView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      uri = Uri(path = Path(s"/shipments/${shipmentId}/deliveries"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ShipmentView](r)
    }
    source.via(flow).runWith(Sink.head)
  }
}