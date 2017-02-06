package com.ecommerce.common.clientactors.http

import java.util.UUID
import com.ecommerce.common.views.RequestViews
import com.ecommerce.common.views.ResponseViews
import scala.concurrent.Future
import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Sink, Source}
import de.heikoseeberger.akkahttpcirce.CirceSupport

/**
  * Created by lukewyman on 2/5/17.
  */
object ShoppingCartHttpClient {

  def props = Props(new ShoppingCartHttpClient)

  def name = "shoppingcart-manager"

  case class GetShoppingCart(id: UUID)
  case class CreateShoppingCart(shoppingCartId: UUID, customerId: UUID)
  case class AddItem(shoppingCartId: UUID, itemId: UUID, count: Int)
  case class RemoveItem(shoppingCartId: UUID, itemID: UUID)
  case class ClearCart(shoppingCartId: UUID)
}

class ShoppingCartHttpClient extends Actor with ActorLogging with ShoppingCartHttpClientApi {
  import ShoppingCartHttpClient._
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
      addItem(scid, iid, AddItemView(c, false)).pipeTo(sender())
  }

}

trait ShoppingCartHttpClientApi extends HttpClient {
  import CirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._
  import RequestViews._
  import ResponseViews._
  import HttpClient._

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
