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

  case class ManagerCommand(cmd: Command, id: Long, replyTo: ActorRef)
  case class ManagerEvent(id: Long, event: Event)
  case class ManagerQuery(qry: Query, id: Long, replyTo: ActorRef)
  case class ManagerResult(id: Long, result: Result)
  case class ManagerRejection(id: Long, reason: String)
}

class BackorderManager extends PersistentActor {
  import BackorderManager._

  override def persistenceId = context.self.path.name

  var backorder = Backorder.empty

  def receiveCommand = {
    case ManagerCommand(cmd, id, replyTo) => {
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
          replyTo ! ManagerEvent(id, event)
        }
      } catch {
        case ex: IllegalArgumentException => replyTo ! ManagerRejection(id, ex.getMessage)
      }
    }
    case ManagerQuery(qry, id, replyTo) => {
      try {
        val query = qry match {
          case GetBackorder(i) => GetBackorderResult(i)
        }
      } catch {
        case ex: IllegalArgumentException => replyTo ! ManagerRejection(id, ex.getMessage)
      }
    }
  }

  def receiveRecover = {
    case e: Event => backorder = backorder.applyEvent(e)
  }
}
