package com.ecommerce.inventory.backend

import Stock._

/**
  * Created by lukewyman on 12/18/16.
  */
trait InventoryItemMessage {
  def inventoryItem: ItemRef
}

sealed trait Command extends InventoryItemMessage

sealed trait Query extends InventoryItemMessage
