package com.ecommerce.inventory.backend

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import com.ecommerce.inventory.backend.StockMessage._

/**
  * Created by lukewyman on 12/18/16.
  */
object StockManager {
  import StockMessage._

  def props = Props(new StockManager)

  def name = "stock-manager"

  case class Rejection(reason: String)

}

class StockManager extends PersistentActor {
  import StockManager._

  override def persistenceId = context.self.path.name

  var stock = Stock.empty

  def receiveCommand = {
    case cmd: Command => {
      try {
        val event = cmd match {
          case SetProduct(i) => ProductChanged(i)
          case AcceptShipment(i, s) => ShipmentAccepted(i, s)
          case HoldForCustomer(i, sc, c) => HeldForCustomer(i, sc, c)
          case Checkout(i, sc) => CheckedOut(i, sc)
        }
        stock = stock.applyEvent(event)

        persist(event) { _ =>
          sender() ! event
        }
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
    }
    case qry: Query => {
      val result = qry match {
        case GetStock(i) => GetStockResult(i)
      }
      sender() ! result
    }
  }

  def receiveRecover = {
    case e: Event => stock = stock.applyEvent(e)
  }
}
