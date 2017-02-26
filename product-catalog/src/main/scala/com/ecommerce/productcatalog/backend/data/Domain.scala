package com.ecommerce.productcatalog.backend.data

import java.util.UUID

/**
  * Created by lukewyman on 2/24/17.
  */

case class Category(
                     categoryId: UUID,
                     categoryName: String
                   )

case class Manufacturer(
                         manufactorerId: UUID,
                         manufacturerName: String
                       )


case class Product(
                    productId: UUID,
                    productCode: String,
                    displayName: String,
                    description: String,
                    price: Double,
                    category: Category,
                    manufacturer: Manufacturer
                  )


