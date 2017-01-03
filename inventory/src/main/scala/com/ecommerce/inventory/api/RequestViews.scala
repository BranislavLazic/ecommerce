package com.ecommerce.inventory.api

import java.time.ZonedDateTime
import java.util.UUID

import com.ecommerce.inventory.backend.InventoryItem.GetItemResult
/**
  * Created by lukewyman on 12/18/16.
  */
object RequestViews {
  case class CreateItemView(id: UUID)
  case class HoldItemsView(stockCount: Int, backorderCount: Int)
  case class AcceptShipmentView(id: UUID, date: String, count: Int)
  case class AcknowledgeShipmentView(id: UUID, expectedDate: String, count: Int)
  case class PaymentView(id: UUID)
}

object ResponseViews {
  case class InventoryItemView(id: UUID)
  case class Hold(inventoryId: UUID,  shoppingCartid: UUID, stockCount: Int, backorderCount: Int)
  case class Shipment(id: UUID, date: String, count: Int)
  case class ShipmentAcknowlidedgement(id: UUID, expectedDate: String, count: Int)
  case class Item(id: UUID)
}

object ResponseMappers {
  import ResponseViews._

  def mapToInventoryItem(gir: GetItemResult): InventoryItemView = ???

}