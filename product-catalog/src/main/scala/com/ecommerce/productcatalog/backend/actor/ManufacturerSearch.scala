package com.ecommerce.productcatalog.backend.actor

import akka.actor.{Props, Actor, ActorLogging}
import com.ecommerce.productcatalog.backend.data.ManufacturerQueries
import com.ecommerce.common.clientactors.protocols.ProductProtocol

/**
  * Created by lukewyman on 2/26/17.
  */
object ManufacturerSearch {
  def props = Props[ManufacturerSearch]
}

class ManufacturerSearch extends Actor with ActorLogging with ManufacturerQueries {

  import context.dispatcher
  import akka.pattern.pipe
  import ProductProtocol._

  def receive = {
    case GetManufacturerById(mid) =>
      getById(mid.id).pipeTo(sender())
      kill()
    case GetManufacturers =>
      getAll.pipeTo(sender())
      kill()
  }

  def kill() = context.stop(self)
}
