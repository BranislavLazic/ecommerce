package com.ecommerce.orchestrator.backend.actor.httpclient

import java.util.UUID

import akka.actor.{Props, Actor}

/**
  * Created by lukewyman on 1/10/17.
  */
object InventoryClient {

  def props = Props(new InventoryClient)

  def name = "inventory-client"

  case class HoldItem(itemId: UUID, shoppingCartId: UUID, count: Int)
  case class MakeReservation(itemId: UUID, customerId: UUID, count: Int)
}

class InventoryClient extends Actor {

  def receive = ???
}
