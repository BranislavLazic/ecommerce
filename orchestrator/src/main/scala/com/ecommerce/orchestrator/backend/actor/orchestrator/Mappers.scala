package com.ecommerce.orchestrator.backend.actor.orchestrator

import com.ecommerce.common.views.InventoryResponse
import com.ecommerce.common.views.ReceivingResponse
import com.ecommerce.orchestrator.backend.actor.orchestrator.ResponseViews

/**
  * Created by lukewyman on 2/8/17.
  */

object Mappers {
  import InventoryResponse._
  import ReceivingResponse._
  import ResponseViews._

  def mapToReceivingSummaryView(shipmentView: ShipmentView, inventoryItemView: InventoryItemView): ReceivingSummaryView = ???
}
