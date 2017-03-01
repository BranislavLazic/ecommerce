package com.ecommerce.common.clientactors.http

import akka.actor.{ActorLogging, Props, Actor}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.{Uri, HttpMethods, HttpRequest}
import akka.stream.scaladsl.{Sink, Source}
import com.ecommerce.common.clientactors.protocols.ProductProtocol
import com.ecommerce.common.identity.Identity
import com.ecommerce.common.views.ProductResponse
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.Future

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductHttpClient {

  def props = Props(new ProductHttpClient)
}

class ProductHttpClient extends Actor with ActorLogging with ProductHttpClientApi {
  import ProductProtocol._
  import akka.pattern.pipe

  implicit def executionContext = context.dispatcher
  implicit def system = context.system

  def receive = {
    case GetProductByProductId(pid) =>
      getProductByProductId(pid).pipeTo(sender())
    case GetProductsByCategory(cid) =>
      getProductsByCategoryId(cid)
    case GetProductsBySearchString(ocid, ss) =>
      getProductsBySearchString(ocid, ss)
  }

}

trait ProductHttpClientApi extends HttpClient{
  import CirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._
  import Identity._
  import ProductResponse._
  import HttpClient._

  def getProductByProductId(productId: ProductRef): Future[HttpClientResult[ProductView]] = {
    val source = Source.single(HttpRequest(method = HttpMethods.GET,
      uri = Uri(path = Path(s"/products/${productId.toString}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[ProductView](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def getProductsByCategoryId(categoryId: CategoryRef): Future[HttpClientResult[Seq[ProductView]]] = {
    val source = Source.single(HttpRequest(method = HttpMethods.GET,
      uri = Uri(path = Path(s"/products/${categoryId.toString}"))))
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[Seq[ProductView]](r)
    }
    source.via(flow).runWith(Sink.head)
  }

  def getProductsBySearchString(categoryId: Option[CategoryRef], searchString: String): Future[HttpClientResult[Seq[ProductView]]] = {
    val source = Source.single(HttpRequest(method = HttpMethods.GET,
      uri = Uri(path = Path(s"/products/")))) // TODO: Figure out how to get search parameters in here.
    val flow = http.outgoingConnection(host = "localhost", port = 8000).mapAsync(1) { r =>
      deserialize[Seq[ProductView]](r)
    }
    source.via(flow).runWith(Sink.head)
  }

}
