package com.ecommerce.shoppingcart.backend

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.persistence.PersistentActor
import com.ecommerce.common.identity.Identity.ShoppingCartRef
/**
  * Created by lukewyman on 12/16/16.
  * Domain Pattern and Cluster Sharding from "Reactive Design Patterns", Manning
  */
object ShoppingCartManager {
  import ShoppingCart._

  def props = Props(new ShoppingCartManager)
  def name(scr: ShoppingCartRef) = scr.id.toString

  val regionName: String = "shoppingcarts"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: Command => (cmd.shoppingCartId.id.toString, cmd)
    case qry: Query => (qry.shoppingCartId.id.toString, qry)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: Command => toHex(cmd.shoppingCartId.id.hashCode & 255)
    case qry: Query => toHex(qry.shoppingCartId.id.hashCode & 255)
  }

  private def toHex(b: Int) =
    new java.lang.StringBuilder(2)
      .append(hexDigits(b >> 4))
      .append(hexDigits(b & 15))
      .toString

  private val hexDigits = "0123456789ABCDEF"
}

class ShoppingCartManager extends PersistentActor with ActorLogging {

  override def persistenceId = context.self.path.name
  log.info(s"Persistence Id: $persistenceId")

  var shoppingCart = ShoppingCart.empty

  def receiveCommand = {
    case cmd: Command =>
      log.info(s"receiveCommand $cmd")
      try {
        val event = cmd match {
          case SetOwner(cart, owner) => OwnerChanged(cart, owner)
          case AddItem(cart, item, count) => ItemAdded(cart, item, count)
          case RemoveItem(cart, item) => ItemRemoved(cart, item)
        }
        shoppingCart = shoppingCart.applyEvent(event)

        persist(event) { _ =>
          sender() ! GetShoppingCartResult(ShoppingCartRef(UUID.fromString(persistenceId)), shoppingCart)
        }
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
    case qry: Query =>
      try {
        val result = qry match {
          case GetItems(id) => GetShoppingCartResult(id, shoppingCart)
        }
        sender() ! result
      } catch {
        case ex: IllegalArgumentException => sender() ! Rejection(ex.getMessage)
      }
  }

  def receiveRecover = {
    case e: Event =>
      log.info(s"receiveRecover event: $e")
      shoppingCart = shoppingCart.applyEvent(e)
  }
}
