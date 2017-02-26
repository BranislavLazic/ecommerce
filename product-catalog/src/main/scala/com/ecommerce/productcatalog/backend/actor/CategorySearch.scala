package com.ecommerce.productcatalog.backend.actor

import akka.actor.{Props, ActorLogging, Actor}
import com.ecommerce.common.clientactors.protocols.ProductProtocol
import com.ecommerce.productcatalog.backend.data.CategoryQueries

/**
  * Created by lukewyman on 2/24/17.
  */
object CategorySearch {
  def props = Props[CategorySearch]
}

class CategorySearch extends Actor with ActorLogging with CategoryQueries {

  import akka.pattern.pipe
  import context.dispatcher
  import ProductProtocol._

  def receive = {
    case GetCategoryById(cid) =>
      getById(cid.id).pipeTo(sender())
      kill()
    case GetCategories =>
      getAll.pipeTo(sender())
      kill()
  }

  def kill() = context.stop(self)
}
