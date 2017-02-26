package com.ecommerce.orchestrator.backend.orchestrator

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout
import cats.{Traverse, Applicative}
import cats.data._
import cats.implicits._
import cats._
import com.ecommerce.common.clientactors.http.HttpClient
import com.ecommerce.common.views.InventoryResponse.InventoryItemView
import com.ecommerce.common.views.ProductResponse.ProductView
import com.ecommerce.orchestrator.backend.Mappers
import com.ecommerce.orchestrator.backend.ResponseViews.ProductSummaryView
import com.ecommerce.orchestrator.backend.orchestrator.ProductOrchestrator.{SearchBySearchString, SearchByCategoryId, SearchByProductId}
import scala.concurrent.Future
import scala.concurrent.duration._
import com.ecommerce.common.identity.Identity._
import com.ecommerce.orchestrator.backend.clientapi.{InventoryApi, ProductApi}

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductOrchestrator {

  def props = Props(new ProductOrchestrator)

  case class SearchByProductId(productId: ProductRef)
  case class SearchByCategoryId(categoryId: CategoryRef)
  case class SearchBySearchString(categoryId: Option[CategoryRef], searchString: String)
}

class ProductOrchestrator extends Actor with ActorLogging
  with ProductApi
  with InventoryApi {

  import akka.pattern.pipe
  import Mappers._
  import HttpClient._

  implicit def executionContext = context.dispatcher
  implicit def timeout: Timeout = Timeout(3 seconds)

  def receive = {
    case SearchByProductId(pid) =>
      val p = EitherT(getProductByProductId(pid))
      val i = EitherT(getInventoryItem(pid))
      Applicative[EitherT[Future, HttpClientError, ?]].map2(p, i)(mapToProductSummaryView)
        .value.pipeTo(sender())
      kill()
    case SearchByCategoryId(cid) =>
      getMergedProductSummary(getProductsByCategoryId(cid)).value.pipeTo(sender())
      kill()
    case SearchBySearchString(ocid, ss) =>
      getMergedProductSummary(getProductsBySearchString(ocid, ss)).value.pipeTo(sender())
      kill()
  }

  private def getMergedProductSummary(productList: Future[Either[HttpClientError, List[ProductView]]]): EitherT[Future, HttpClientError, List[ProductSummaryView]] = {
    val plET: EitherT[Future, HttpClientError, List[ProductView]] = EitherT(productList)
    val ilET: EitherT[Future, HttpClientError, List[InventoryItemView]] =
      plET.flatMap(_.traverseU(p => EitherT(getInventoryItem(ProductRef(p.productId)))))

    Applicative[EitherT[Future, HttpClientError, ?]].map2(plET, ilET) { (pl, il) =>
      (pl, il).zipped map {(p, i) => mapToProductSummaryView(p, i)}
    }
  }

  def kill() = {
    context.children foreach { context.stop(_) }
    context.stop(self)
  }
}
