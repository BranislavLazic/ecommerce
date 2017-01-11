package com.ecommerce.orchestrator.backend.actor.httpclient

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, StatusCodes, HttpResponse, HttpRequest}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}

/**
  * Created by lukewyman on 1/1/17.
  */
object ShoppingCartClient {

  def props = Props(new ShoppingCartClient)

  def name = "shoppingcart-manager"

  case class CreateShoppingCart(shoppingCartId: UUID, customerId: UUID)
  case class PlaceInCart(shoppingCartId: UUID, itemId: UUID, count: Int)
}

class ShoppingCartClient extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def sendRequest(entity: HttpEntity) = {
    http.singleRequest(HttpRequest(uri = "http://akka.io"))
      .pipeTo(self)
  }

  def receive = {

    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
    case HttpResponse(code, _, _, _) =>
  }
}
