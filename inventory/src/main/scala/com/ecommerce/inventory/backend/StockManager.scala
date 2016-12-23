package com.ecommerce.inventory.backend

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import com.ecommerce.inventory.backend.StockMessage._

/**
  * Created by lukewyman on 12/18/16.
  */
object StockManager {
  import StockMessage._
  import StockManager._

  def props = Props(new StockManager)

  def name = "stock-manager"

  case class ManagerCommand(cmd: Command, id: Long, replyTo: ActorRef)
  case class ManagerEvent(id: Long, event: Event)
  case class ManagerQuery(qry: Query, id: Long, replyTo: ActorRef)
  case class ManagerResult(id: Long, result: Result)
  case class ManagerRejection(id: Long, reason: String)
}

class StockManager extends PersistentActor {
  import StockManager._

  override def persistenceId = context.self.path.name

  var stock = Stock.empty

  def receiveCommand = {
    case ManagerCommand(cmd, id, replyTo) => {
      try {
        val event = cmd match {
          case SetProduct(i) => ProductChanged(i)
          case AcceptShipment(i, s) => ShipmentAccepted(i, s)
          case HoldForCustomer(i, sc, c) => HeldForCustomer(i, sc, c)
          case Checkout(i, sc) => CheckedOut(i, sc)
        }
        stock = stock.applyEvent(event)

        persist(event) { _ =>
          replyTo ! ManagerEvent(id, event)
        }
      } catch {
        case ex: IllegalArgumentException => replyTo ! ManagerRejection(id, ex.getMessage)
      }
    }
    case ManagerQuery(qry, id, replyTo) => {
      val result = qry match {
        case GetStock(i) => GetStockResult(i)
      }
      replyTo ! ManagerResult(id, result)
    }
  }

  def receiveRecover = {
    case e: Event => stock = stock.applyEvent(e)
  }
}
