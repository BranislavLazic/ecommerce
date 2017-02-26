package com.ecommerce.productcatalog.backend.actor

import akka.actor.{ActorLogging, Actor}
import com.ecommerce.common.clientactors.protocols.ProductProtocol.GetProductByProductId
import com.ecommerce.productcatalog.backend.data.{Database, ProductQueries}

/**
  * Created by lukewyman on 2/24/17.
  */
class ProductSearch extends Actor with ActorLogging with ProductQueries with Database {

  import akka.pattern.pipe
  import context.dispatcher

  def receive = {
    case GetProductByProductId(pid) =>
      db.run(getProductById(pid.id)).pipeTo(sender())
  }
}
