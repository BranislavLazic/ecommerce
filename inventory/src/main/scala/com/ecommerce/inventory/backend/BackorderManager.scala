package com.ecommerce.inventory.backend

import akka.actor.Props
import akka.persistence.PersistentActor

/**
  * Created by lukewyman on 12/18/16.
  */
object BackorderManager {
  def props = Props(new BackorderManager)

  def name = "backorder-manager"
}

class BackorderManager extends PersistentActor {

  override def persistenceId = ???

  def receiveCommand = ???

  def receiveRecover = ???
}
