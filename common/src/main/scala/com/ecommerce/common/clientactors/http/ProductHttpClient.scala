package com.ecommerce.common.clientactors.http

import akka.actor.{Props, Actor}
import com.ecommerce.common.clientactors.http.HttpClient.HttpClientResult
import com.ecommerce.common.clientactors.protocols.ProductProtocol.{GetProductBySearchString, GetProductByCategory, GetProductByProductId}
import com.ecommerce.common.identity.Identity
import com.ecommerce.common.views.ProductResponse

import scala.concurrent.Future

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductHttpClient {

  def props = Props(new ProductHttpClient)

  def name = "product-client"
}

class ProductHttpClient extends Actor {

  def receive = {
    case GetProductByProductId(pid) =>

    case GetProductByCategory(cid) =>

    case GetProductBySearchString(ocid, ss) =>

  }

}

trait ProductHttpClientApi {
  import Identity._
  import ProductResponse._

  def getProductByProductId(productId: ProductRef): Future[HttpClientResult[ProductView]] = ???

  def getProductsByCategoryId(categoryId: CategoryRef): Future[HttpClientResult[Seq[ProductView]]] = ???

  def getProductsBySearchString: Future[HttpClientResult[Seq[ProductView]]] = ???

}
