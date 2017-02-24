package com.ecommerce.orchestrator.backend.clientapi

import akka.actor.Actor
import com.ecommerce.common.clientactors.http.ProductHttpClient

/**
  * Created by lukewyman on 2/24/17.
  */
trait ProductApi { this: Actor =>

  def productClient = context.actorOf(ProductHttpClient.props, ProductHttpClient.name)

  def getProductByProductId = ???

  def getProductsByCategoryId = ???

  def getProductsBySearchString = ???
}
