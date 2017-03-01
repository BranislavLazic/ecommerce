package com.ecommerce.orchestrator.backend.clientapi

import akka.actor.Actor
import akka.util.Timeout
import com.ecommerce.common.clientactors.http.HttpClient.HttpClientResult
import com.ecommerce.common.clientactors.protocols.ProductProtocol
import com.ecommerce.common.clientactors.http.{HttpClient, ProductHttpClient}
import com.ecommerce.common.identity.Identity
import com.ecommerce.common.views.ProductResponse

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lukewyman on 2/24/17.
  */
trait ProductApi { this: Actor =>
  import Identity._
  import ProductResponse._
  import ProductProtocol._
  import HttpClient._
  import akka.pattern.ask

  implicit def executionContext: ExecutionContext
  implicit def timeout: Timeout

  def productClient = context.actorOf(ProductHttpClient.props)

  def getProductByProductId(productId: ProductRef): Future[HttpClientResult[ProductView]]  =
    productClient.ask(GetProductByProductId(productId)).mapTo[HttpClientResult[ProductView]]

  def getProductsByCategoryId(categoryId: CategoryRef): Future[HttpClientResult[List[ProductView]]] =
    productClient.ask(GetProductsByCategory(categoryId)).mapTo[HttpClientResult[List[ProductView]]]

  def getProductsBySearchString(categoryId: Option[CategoryRef], searchString: String): Future[HttpClientResult[List[ProductView]]] =
    productClient.ask(GetProductsBySearchString(categoryId, searchString)).mapTo[HttpClientResult[List[ProductView]]]
}
