package com.ecommerce.productcatalog.backend.actor

import akka.actor.{Props, ActorLogging, Actor}
import com.ecommerce.common.clientactors.protocols.ProductProtocol
import com.ecommerce.productcatalog.backend.data.{Database, ProductQueries}

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductSearch {
  def props = Props[ProductSearch]
}

class ProductSearch extends Actor with ActorLogging with ProductQueries {

  import akka.pattern.pipe
  import context.dispatcher
  import ProductProtocol._

  def receive = {
    case GetProductByProductId(pid) =>
      getProductById(pid.id).pipeTo(sender())
      kill()
    case GetProductByCategory(cid) =>
      getProductsByCategory(cid.id).pipeTo(sender())
      kill()
    case GetProductBySearchString(ocid, ss) =>
      getProductsBySearchString(ocid.map(_.id), ss).pipeTo(sender())
      kill()
  }

  def kill() = context.stop(self)
}
