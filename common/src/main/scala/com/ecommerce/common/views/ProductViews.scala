package com.ecommerce.common.views

import java.util.UUID

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductResponse {

  case class ProductView(productId: UUID, productCode: String, displayName: String, manufacturerId: UUID, description: String)
}
