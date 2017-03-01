package com.ecommerce.productcatalog.api

import com.ecommerce.common.views.ProductResponse
import com.ecommerce.productcatalog.backend.data.{Manufacturer, Category, Product}

/**
  * Created by lukewyman on 2/26/17.
  */
object ResponseMappers {
  import ProductResponse._

  def mapToCategoryView(category: Category): CategoryView =
    CategoryView(
      category.categoryId,
      category.categoryName
    )

  def mapToManufacturerView(manufacturer: Manufacturer): ManufacturerView =
    ManufacturerView(
      manufacturer.manufactorerId,
      manufacturer.manufacturerName
    )

  def maptoProductView(product: Product): ProductView =
    ProductView(
      product.productId,
      product.productCode,
      product.displayName,
      product.description,
      product.price,
      mapToCategoryView(product.category),
      mapToManufacturerView(product.manufacturer)
    )
}
