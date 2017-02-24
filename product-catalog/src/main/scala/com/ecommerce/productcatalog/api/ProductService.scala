package com.ecommerce.productcatalog.api

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.CirceSupport

import scala.concurrent.ExecutionContext
import scala.util.Try

/**
  * Created by lukewyman on 2/23/17.
  */
case class ProductService(val system: ActorSystem, val requestTimeout: Timeout) {
  val executionContext = system.dispatcher
}

trait ProductRoutes {

  import CirceSupport._
  import Directives._
  import StatusCodes._
  import io.circe.generic.auto._

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    getProductByProductId ~
    getProductsByCategoryId ~
    getProductsBySearchString

  def getProductByProductId: Route = {
    get {
      pathPrefix("products" / ProductId ) { productId =>
        pathEndOrSingleSlash {
          complete(OK)
        }
      }
    }
  }

  def getProductsByCategoryId: Route = {
    get {
      pathPrefix("products" / CategoryId) { categoryId =>
        pathEndOrSingleSlash {
          complete(OK)
        }
      }
    }
  }

  //TODO: Figure out how to do query parameters with Akka HTTP
  def getProductsBySearchString: Route = {
    get {
      pathPrefix("products") {
        pathEndOrSingleSlash {
          complete(OK)
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ProductId = IdSegment
  val CategoryId = IdSegment
}
