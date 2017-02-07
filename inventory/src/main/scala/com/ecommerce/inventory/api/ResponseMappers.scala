package com.ecommerce.inventory.api

import com.ecommerce.common.views.InventoryResponse
import com.ecommerce.inventory.backend.InventoryItemManager.GetItemResult

/**
  * Created by lukewyman on 12/18/16.
  */

object ResponseMappers {
  import InventoryResponse._

  def mapToInventoryItem(gir: GetItemResult): InventoryItemView = ???

}