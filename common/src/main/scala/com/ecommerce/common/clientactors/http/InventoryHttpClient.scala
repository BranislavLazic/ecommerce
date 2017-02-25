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
import com.ecommerce.common.identity.Identity

/**
  * Created by lukewyman on 2/5/17.
  */
object InventoryHttpClient {

  def props = Props(new InventoryHttpClient)
}

class InventoryHttpClient extends Actor with ActorLogging with InventoryHttpClientApi {
  import InventoryProtocol._
  import InventoryRequest._
  import akka.pattern.pipe

  implicit def executionContext = context.dispatcher
  implicit def system = context.system

  def receive = {
    case CreateItem(pid) =>
      createItem(CreateItemView(pid.id)).pipeTo(sender())
    case GetItem(pid) =>
      getItem(pid).pipeTo(sender())
    case ReceiveSupply(pid, sid, d, c) =>
      acceptShipment(pid, ReceiveSupplyView(sid.id, null, d, c)).pipeTo(sender())
    case NotifySupply(pid, sid, ed, c) =>
      acknowledgeShipment(pid, NotifySupplyView(sid.id, ed, c)).pipeTo(sender())
    case HoldItem(pid, scid, c) =>
      holdItem(pid, scid, HoldItemView(c)).pipeTo(sender())
    case ReserveItem(pid, cid, c) =>
      reserveItem(pid, ReserveItemView(cid.id, null, null, c)).pipeTo(sender())
    case ReleaseItem(pid, scid) =>
      abandonCart(pid, scid).pipeTo(sender())
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
  import Identity._

  def createItem(civ: CreateItemView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, civ.asJson.toString()),
      uri = Uri(path = Path("/items"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def getItem(productId: ProductRef): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.GET,
      uri = Uri(path = Path(s"/items/${productId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 9000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def acceptShipment(productId: ProductRef, asv: ReceiveSupplyView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, asv.asJson.toString()),
      uri = Uri(path = Path(s"/items/${productId}/shipments"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def acknowledgeShipment(productId: ProductRef, asv: NotifySupplyView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, asv.asJson.toString()),
      uri = Uri(path = Path(s"/items/${productId}/acknowledgements"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def holdItem(productId: ProductRef, shoppingCartId: ShoppingCartRef, hiv: HoldItemView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, hiv.asJson.toString()),
      uri = Uri(path = Path(s"/items/${productId}/shoppingcarts/${shoppingCartId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) {r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def reserveItem(productId: ProductRef, riv: ReserveItemView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, riv.asJson.toString()),
      uri = Uri(path = Path(s"items/${productId}/customers"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def abandonCart(productId: ProductRef, shoppingCartId: ShoppingCartRef): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.DELETE,
      uri = Uri(path = Path(s"items/${productId}/shoppingcarts/${shoppingCartId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def checkout(productId: ProductRef, shoppingCartId: ShoppingCartRef, cv: CheckoutView): Future[HttpClientResult[InventoryItemView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, cv.asJson.toString()),
      uri = Uri(path = Path(s"items/${productId}/shoppingcarts/${shoppingCartId}/payments"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[InventoryItemView](r)
    }
    source.via(flow).runWith(Sink.head)
  }
}

