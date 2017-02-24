package com.ecommerce.orchestrator.backend.clientapi

import akka.actor.Actor
import com.ecommerce.common.clientactors.http.HttpClient.HttpClientResult
import com.ecommerce.common.clientactors.http.ProductHttpClient
import com.ecommerce.common.identity.Identity
import com.ecommerce.common.views.ProductResponse

import scala.concurrent.Future

/**
  * Created by lukewyman on 2/24/17.
  */
trait ProductApi { this: Actor =>
  import Identity._
  import ProductResponse._

  def productClient = context.actorOf(ProductHttpClient.props, ProductHttpClient.name)

  def getProductByProductId(productId: ProductRef): Future[HttpClientResult[ProductView]]  = ???

  def getProductsByCategoryId(categoryId: CategoryRef): Future[HttpClientResult[Seq[ProductView]]] = ???

  def getProductsBySearchString(categoryId: Option[CategoryRef], searchString: String): Future[HttpClientResult[Seq[ProductView]]] = ???
}
