package com.ecommerce.common.clientactors.protocols

import com.ecommerce.common.identity.Identity.{CategoryRef, ProductRef}

/**
  * Created by lukewyman on 2/24/17.
  */
object ProductProtocol {

  case class GetProductByProductId(productId: ProductRef)
  case class GetProductByCategory(categoryId: CategoryRef)
  case class GetProductBySearchString(categoryId: Option[CategoryRef], SearchString: String)
}