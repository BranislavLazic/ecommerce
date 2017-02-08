package com.ecommerce.receiving.backend

import akka.actor.Props
import akka.persistence.PersistentActor
import com.ecommerce.receiving.backend.Shipment.ShipmentRef

/**
  * Created by lukewyman on 2/7/17.
  */
object ShipmentManager {

  val props = Props()

  def name(sr: ShipmentRef) = sr.id.toString


}

class ShipmentManager extends PersistentActor {

  def persistenceId = ???

  def receiveCommand = ???

  def receiveRecover = ???
}
