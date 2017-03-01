package com.ecommerce.common.views

import java.util.UUID

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductResponse {

  case class CategoryView(
                           categoryId: UUID,
                           categoryName: String
                         )

  case class ManufacturerView(
                           manufacturerId: UUID,
                           manufacturerName: String
                         )

  case class ProductView(
                          productId: UUID,
                          productCode: String,
                          displayName: String,
                          description: String,
                          price: Double,
                          category: CategoryView,
                          manufacturer: ManufacturerView
                        )
}
