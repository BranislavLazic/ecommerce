package com.ecommerce.inventory.backend

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import com.ecommerce.inventory.backend.BackorderMessage._

/**
  * Created by lukewyman on 12/18/16.
  */
object BackorderManager {
  def props = Props(new BackorderManager)

  def name = "backorder-manager"

  case class Rejection(reason: String)

}

class BackorderManager extends PersistentActor {
  import BackorderManager._

  override def persistenceId = context.self.path.name

  var backorder = Backorder.empty

  def receiveCommand = {
    case cmd: Command => {
      try {
        val event = cmd match {
          case SetProduct(i) => ProductChanged(i)
          case AcknowledgeShipment(i, s) => ShipmentAcknowledged(i, s)
          case AcceptShipment(i, s) => ShipmentAcknowledged(i, s)
          case MakeReservation(i, r, c) => ReservationMade(i, r, c)
          case AbandonCart(i, sc) => CartAbandoned(i, sc)
        }
        backorder = backorder.applyEvent(event)

        persist(event) {_ =>
          sender() ! event
        }
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
    }
    case qry: Query => {
      try {
        val query = qry match {
          case GetBackorder(i) => GetBackorderResult(i)
        }
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
    }
  }

  def receiveRecover = {
    case e: Event => backorder = backorder.applyEvent(e)
  }
}
