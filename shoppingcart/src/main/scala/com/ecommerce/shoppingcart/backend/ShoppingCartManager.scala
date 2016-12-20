package com.ecommerce.shoppingcart.backend

import akka.actor.Props
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor

/**
  * Created by lukewyman on 12/16/16.
  */
object ShoppingCartManager {
  import ShoppingCart._

  def props = Props(new ShoppingCartManager)
  def name(scr: ShoppingCartRef) = scr.id.toString

  case class Rejection(reason: String)

  val regionName: String = "shoppingcarts"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: Command => (cmd.shoppingCart.id.toString, cmd)
    case qry: Query => (qry.shoppingCart.id.toString, qry)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: Command => toHex(cmd.shoppingCart.id.hashCode & 255)
    case qry: Query => toHex(qry.shoppingCart.id.hashCode & 255)
  }

  private def toHex(b: Int) =
    new java.lang.StringBuilder(2)
      .append(hexDigits(b >> 4))
      .append(hexDigits(b & 15))
      .toString

  private val hexDigits = "0123456789ABCDEF"
}

class ShoppingCartManager extends PersistentActor {
  import ShoppingCartManager.Rejection

  override def persistenceId = context.self.path.name

  var shoppingCart = ShoppingCart.empty

  def receiveCommand = {
    case cmd: Command =>
      try {
        val event = cmd match {
          case SetOwner(cart, owner) => OwnerChanged(cart, owner)
          case AddItem(cart, item, count) => ItemAdded(cart, item, count)
          case RemoveItem(cart, item, count) => ItemRemoved(cart, item, count)
        }
        shoppingCart = shoppingCart.applyEvent(event)

        persist(event) { _ =>
          sender() ! event
        }
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
    case qry: Query =>
      try {
        val result = qry match {
          case GetItems(cart) => GetItemsResult(cart, shoppingCart.items)
        }
        sender() ! result
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
  }

  def receiveRecover = {
    case e: Event => shoppingCart = shoppingCart.applyEvent(e)
  }
}
