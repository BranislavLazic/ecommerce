package com.ecommerce.inventory.api

import java.util.UUID
import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import akka.http.scaladsl.server.{Route, Directives}
import akka.http.scaladsl.model.StatusCodes._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext
import scala.util.Try
import com.ecommerce.inventory.backend.Identity.{PaymentRef, ShoppingCartRef, ShipmentRef, ItemRef}
import com.ecommerce.inventory.backend.InventoryItem._

/**
  * Created by lukewyman on 12/18/16.
  */
class InventoryService(val inventoryItems: ActorRef, val system: ActorSystem, val requestTimeout: Timeout) extends InventoryRoutes {
  val executionContext = system.dispatcher
}

trait InventoryRoutes {
  import Directives._
  import CirceSupport._
  import io.circe.generic.auto._
  import RequestViews._
  import ResponseMappers._

  def inventoryItems: ActorRef

  implicit def requestTimeout: Timeout
  implicit def executionContext: ExecutionContext

  def routes: Route =
    checkout ~
    abandonCart ~
    placeItemInCart ~
    acceptShipment ~
    acknowledgeShipment ~
    getItem ~
    createItem

  def createItem: Route = {
    post {
      pathPrefix("shoppingcarts") {
        pathEndOrSingleSlash {
          entity(as[CreateItemView]) { civ =>
            val setProduct = SetProduct(ItemRef(civ.id))
            inventoryItems ! setProduct
            complete(OK)
          }
        }
      }
    }
  }

  def getItem: Route = {
    get {
      pathPrefix("items" / ItemId) { itemId =>
        pathEndOrSingleSlash {
          onSuccess(inventoryItems.ask(GetItem(ItemRef(itemId))).mapTo[GetItemResult]) {
            case result => complete(mapToInventoryItem(result))
          }
        }
      }
    }
  }

  def acceptShipment: Route = {
    post {
      pathPrefix("items" / ItemId / "shipments") { itemId =>
        pathEndOrSingleSlash {
          entity(as[AcceptShipmentView]) { asv =>
            val shipment = AcceptShipment(ItemRef(itemId), ShipmentRef(asv.id, DateTime.parse(asv.date), asv.count))
            inventoryItems ! shipment
            complete(OK)
          }
        }
      }
    }
  }

  def acknowledgeShipment: Route = {
    post {
      pathPrefix("items" / ItemId / "acknowledgements") { itemId =>
        pathEndOrSingleSlash {
          entity(as[AcknowledgeShipmentView]) { asv =>
            val acknowledgement =
              AcknowledgeShipment(ItemRef(itemId), ShipmentRef(asv.id, DateTime.parse(asv.expectedDate), asv.count))
            inventoryItems ! acknowledgement
            complete(OK)
          }
        }
      }
    }
  }

  def placeItemInCart: Route = {
    post {
      pathPrefix("items" / ItemId / "shoppingcarts" / ShoppingCartId) { (itemId, shoppingCartId) =>
        pathEndOrSingleSlash {
          entity(as[HoldItemsView]) { hold =>
            val holdItems = HoldItems(ItemRef(itemId), ShoppingCartRef(shoppingCartId), hold.stockCount, hold.backorderCount)
            inventoryItems ! holdItems
            complete(OK)
          }
        }
      }
    }
  }

  def abandonCart: Route = {
    delete {
      pathPrefix("items" / ItemId / "shoppingcarts" / ShoppingCartId) { (itemId, shoppingCartId) =>
        pathEndOrSingleSlash {
          val release = ReleaseItems(ItemRef(itemId), ShoppingCartRef(shoppingCartId))
          inventoryItems ! release
          complete(OK)
        }
      }
    }
  }

  def checkout: Route = {
    post {
      pathPrefix("items" / ItemId / "shoppingcarts" / ShoppingCartId / "payments") { (itemId, shoppingCartId) =>
        pathEndOrSingleSlash {
          entity(as[PaymentView]) { pv =>
            val checkout = Checkout(ItemRef(itemId), ShoppingCartRef(shoppingCartId), PaymentRef(pv.id))
            inventoryItems ! checkout
            complete(OK)
          }
        }
      }
    }
  }

  val IdSegment = Segment.flatMap(id => Try(UUID.fromString(id)).toOption)
  val ItemId = IdSegment
  val ShoppingCartId = IdSegment
}
