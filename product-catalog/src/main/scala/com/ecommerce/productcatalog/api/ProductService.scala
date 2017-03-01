package com.ecommerce.productcatalog.api

import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.util.Timeout
import com.ecommerce.common.clientactors.protocols.ProductProtocol._
import com.ecommerce.common.identity.Identity.{ManufacturerRef, CategoryRef, ProductRef}
import com.ecommerce.productcatalog.backend.actor.{ManufacturerSearch, CategorySearch, ProductSearch}
import com.ecommerce.productcatalog.backend.data.{Product, Category, Manufacturer}
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

  import Directives._
  import StatusCodes._
  import CirceSupport._
  import io.circe.generic.auto._
  import akka.pattern.ask
  import ResponseMappers._

  def system: ActorSystem

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    getProductByProductId ~
    getProductsBySearchString ~
    getProductsByCategoryId ~
    getCategoryById ~
    getCategories ~
    getProductsByManufacturerId ~
    getManufacturerById ~
    getManufacturers

  def getProductByProductId: Route = {
    get {
      pathPrefix("products" / ProductId ) { productId =>
        pathEndOrSingleSlash {
          val productSearch = system.actorOf(ProductSearch.props)
          onSuccess(productSearch.ask(GetProductByProductId(ProductRef(productId))).mapTo[Product]) { p =>
            complete(OK, maptoProductView(p))
          }
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

  def getProductsByCategoryId: Route = {
    get {
      pathPrefix("categories" / CategoryId / "products") { categoryId =>
        pathEndOrSingleSlash {
          val productSearch = system.actorOf(ProductSearch.props)
          onSuccess(productSearch.ask(GetProductsByCategory(CategoryRef(categoryId))).mapTo[List[Product]]) { ps =>
            complete(OK, ps.map(maptoProductView(_)))
          }
        }
      }
    }
  }

  def getCategoryById: Route = {
    get {
      pathPrefix("categories" / CategoryId) { categoryId =>
        pathEndOrSingleSlash {
          val categorySearch = system.actorOf(CategorySearch.props)
          onSuccess(categorySearch.ask(GetCategoryById(CategoryRef(categoryId))).mapTo[Category]) { c =>
            complete(OK, mapToCategoryView(c))
          }
        }
      }
    }
  }

  def getCategories: Route = {
    get {
      pathPrefix("categories") {
        pathEndOrSingleSlash {
          val categorySearch = system.actorOf(CategorySearch.props)
          onSuccess(categorySearch.ask(GetCategories).mapTo[List[Category]]) { cs =>
            complete(OK, cs.map(mapToCategoryView(_)))
          }
        }
      }
    }
  }

  def getProductsByManufacturerId: Route = {
    get {
      pathPrefix("manufacturers" / ManufacturerId / "products") { manufacturerId =>
        pathEndOrSingleSlash {
          val productSearch = system.actorOf(ProductSearch.props)
          onSuccess(productSearch.ask(GetProductsByManufacturer(ManufacturerRef(manufacturerId))).mapTo[List[Product]]) { ps =>
            complete(OK, ps.map(maptoProductView(_)))
          }
        }
      }
    }
  }

  def getManufacturerById: Route = {
    get {
      pathPrefix("manufacturers" / ManufacturerId) { manufacturerId =>
        pathEndOrSingleSlash {
          val manufacturerSearch = system.actorOf(ManufacturerSearch.props)
          onSuccess(manufacturerSearch.ask(GetManufacturerById(ManufacturerRef(manufacturerId))).mapTo[Manufacturer]) { m =>
            complete(OK, mapToManufacturerView(m))
          }
        }
      }
    }
  }

  def getManufacturers: Route = {
    get {
      pathPrefix("manufacturers") {
        pathEndOrSingleSlash {
          val manufacturerSeach = system.actorOf(ManufacturerSearch.props)
          onSuccess(manufacturerSeach.ask(GetManufacturers).mapTo[List[Manufacturer]]) { ms =>
            complete(OK, ms.map(mapToManufacturerView(_)))
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ProductId = IdSegment
  val CategoryId = IdSegment
  val ManufacturerId = IdSegment
}
