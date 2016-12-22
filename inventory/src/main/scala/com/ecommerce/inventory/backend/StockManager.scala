package com.ecommerce.inventory.backend

import akka.actor.Props
import akka.persistence.PersistentActor

/**
  * Created by lukewyman on 12/18/16.
  */
object StockManager {

  def props = Props(new StockManager)

  def name = "stock-manager"


}

class StockManager extends PersistentActor {

  override def persistenceId = ???

  def receiveCommand = ???

  def receiveRecover = ???
}
