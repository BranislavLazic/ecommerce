package com.ecommerce.common.clientactors.http

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.{ActorLogging, Props, Actor}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import de.heikoseeberger.akkahttpcirce.CirceSupport
import scala.concurrent.Future
import com.ecommerce.common.views.InventoryRequest
import com.ecommerce.common.views.InventoryResponse
import com.ecommerce.common.clientactors.protocols.InventoryProtocol

/**
  * Created by lukewyman on 2/5/17.
  */
object InventoryHttpClient {

  def props = Props(new InventoryHttpClient)

  def name = "inventory-client"
}

class InventoryHttpClient extends Actor with ActorLogging with InventoryHttpClientApi {
  import InventoryProtocol._
  import InventoryRequest._
  import akka.pattern.pipe
  implicit def executionContext = context.dispatcher
  implicit def system = context.system

  def receive = {
    case CreateItem(iid) =>
      createItem(CreateItemView(iid)).pipeTo(sender())
    case GetItem(iid) =>
      getItem(iid).pipeTo(sender())
    case ReceiveSupply(iid, sid, d, c) =>
      acceptShipment(iid, ReceiveSupplyView(sid, d, c)).pipeTo(sender())
    case NotifySupply(iid, sid, ed, c) =>
      acknowledgeShipment(iid, NotifySupplyView(sid, ed, c)).pipeTo(sender())
    case HoldItem(iid, scid, c) =>
      holdItem(iid, scid, HoldItemView(c)).pipeTo(sender())
    case ReserveItem(iid, cid, c) =>
      reserveItem(iid, ReserveItemView(cid, null, c)).pipeTo(sender())
    case ReleaseItem(iid, scid) =>
      abandonCart(iid, scid).pipeTo(sender())
    case ClaimItem(iid, scid, pid) =>
      checkout(iid, scid, CheckoutView("")).pipeTo(sender())
  }
}

trait InventoryHttpClientApi extends HttpClient {
  import CirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._
  import io.circe.java8.time._
  import InventoryRequest._
  import InventoryResponse._
  import HttpClient._

  def createItem(civ: CreateItemView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, civ.asJson.toString()),
      uri = Uri(path = Path("/items"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def getItem(itemId: UUID): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.GET,
      uri = Uri(path = Path(s"/items/${itemId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 9000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def acceptShipment(itemId: UUID, asv: ReceiveSupplyView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, asv.asJson.toString()),
      uri = Uri(path = Path(s"/items/${itemId}/shipments"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def acknowledgeShipment(itemId: UUID, asv: NotifySupplyView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, asv.asJson.toString()),
      uri = Uri(path = Path(s"/items/${itemId}/acknowledgements"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def holdItem(itemId: UUID, shoppingCartId: UUID, hiv: HoldItemView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, hiv.asJson.toString()),
      uri = Uri(path = Path(s"/items/${itemId}/shoppingcarts/${shoppingCartId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) {r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def reserveItem(itemId: UUID, riv: ReserveItemView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, riv.asJson.toString()),
      uri = Uri(path = Path(s"items/${itemId}/customers"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def abandonCart(itemId: UUID, shoppingCartId: UUID): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.DELETE,
      uri = Uri(path = Path(s"items/${itemId}/shoppingcarts/${shoppingCartId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def checkout(itemId: UUID, shoppingCartId: UUID, cv: CheckoutView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, cv.asJson.toString()),
      uri = Uri(path = Path(s"items/${itemId}/shoppingcarts/${shoppingCartId}/payments"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }
}

