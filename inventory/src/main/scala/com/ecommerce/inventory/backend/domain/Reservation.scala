package com.ecommerce.inventory.backend.domain

import com.ecommerce.common.identity.Identity.CustomerRef

/**
  * Created by lukewyman on 2/16/17.
  */
case class Reservation(customerId: CustomerRef, shipment: Shipment)