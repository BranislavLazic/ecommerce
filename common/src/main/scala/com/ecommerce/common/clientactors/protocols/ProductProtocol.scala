package com.ecommerce.common.clientactors.protocols

import com.ecommerce.common.identity.Identity

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductProtocol {
  import Identity._

  case class GetProductByProductId(productId: ProductRef)
  case class GetProductsByCategory(categoryId: CategoryRef)
  case class GetProductsBySearchString(categoryId: Option[CategoryRef], SearchString: String)
  case class GetProductsByManufacturer(manufacturerId: ManufacturerRef)
  case class GetCategoryById(categoryId: CategoryRef)
  case object GetCategories
  case class GetManufacturerById(manufacturerId: ManufacturerRef)
  case object GetManufacturers
}