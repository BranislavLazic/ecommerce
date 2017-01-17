package com.ecommerce.orchestrator.backend.actor.httpclient

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import com.ecommerce.orchestrator.backend.actor.httpclient.HttpClient.HttpClientResult
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.Future

/**
  * Created by lukewyman on 1/1/17.
  */
object ShoppingCartClient {

  def props = Props(new ShoppingCartClient)

  def name = "shoppingcart-manager"

  case class GetShoppingCart(id: UUID)
  case class CreateShoppingCart(shoppingCartId: UUID, customerId: UUID)
  case class AddItem(shoppingCartId: UUID, itemId: UUID, count: Int)
}

class ShoppingCartClient extends Actor with ActorLogging with ShoppingCartHttpClient {
  import ShoppingCartClient._
  import RequestViews._
  import akka.pattern.pipe
  implicit def executionContext = context.dispatcher
  implicit def system = context.system

  def receive = {
    case GetShoppingCart(id) =>
      getShoppingCart(id).pipeTo(sender())
    case CreateShoppingCart(scid, cid) =>
      createShoppingCart(CreateShoppingCartView(scid, cid)).pipeTo(sender())
    case AddItem(scid, iid, c) =>
      addItem(scid, iid, AddItemView(c)).pipeTo(sender())
  }

}

trait ShoppingCartHttpClient extends HttpClient {
  import CirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._
  import RequestViews._
  import ResponseViews._

  def getShoppingCart(shoppingCartId: UUID): Future[HttpClientResult[ShoppingCartView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.GET,
      uri = Uri(path = Path(s"/shoppingcarts/${shoppingCartId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ShoppingCartView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def createShoppingCart(cscv: CreateShoppingCartView): Future[HttpClientResult[ShoppingCartView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, cscv.asJson.toString()),
      uri = Uri(path = Path("/shoppingcarts"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ShoppingCartView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def addItem(shoppingCartId: UUID, itemId: UUID, aiv: AddItemView): Future[HttpClientResult[ShoppingCartView]] = {

    val source = Source.single(HttpRequest(method = HttpMethods.POST,
      entity = HttpEntity(ContentTypes.`application/json`, aiv.asJson.toString()),
      uri = Uri(path = Path(s"/shoppingcarts/${shoppingCartId}/items/${itemId}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ShoppingCartView](r)
    }
    source.via(flow).runWith(Sink.head)
  }
}
