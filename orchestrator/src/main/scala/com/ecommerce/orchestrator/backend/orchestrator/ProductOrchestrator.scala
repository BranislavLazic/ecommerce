package com.ecommerce.orchestrator.backend.orchestrator

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout
import com.ecommerce.orchestrator.backend.orchestrator.ProductOrchestrator.{SearchBySearchString, SearchByCategoryId, SearchByProductId}
import scala.concurrent.duration._
import com.ecommerce.common.identity.Identity._
import com.ecommerce.orchestrator.backend.clientapi.{InventoryApi, ProductApi}

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductOrchestrator {

  def props = Props(new ProductOrchestrator)

  def name = "product-orchestrator"

  case class SearchByProductId(productId: ProductRef)
  case class SearchByCategoryId(categoryId: CategoryRef)
  case class SearchBySearchString(categoryId: Option[CategoryRef], searchString: String)
}

class ProductOrchestrator extends Actor with ActorLogging
  with ProductApi
  with InventoryApi {

  implicit def executionContext = context.dispatcher
  implicit def timeout: Timeout = Timeout(3 seconds)

  def receive = {
    case SearchByProductId(pid) =>

    case SearchByCategoryId(cid) =>

    case SearchBySearchString(ocid, ss) =>

  }

  def kill() = log.info("stopping children and self after message") // TODO: implementation to stop http client actors and self
}
