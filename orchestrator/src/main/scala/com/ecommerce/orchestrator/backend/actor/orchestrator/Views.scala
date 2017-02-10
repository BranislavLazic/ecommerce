package com.ecommerce.orchestrator.backend.actor.orchestrator

import java.time.ZonedDateTime
import java.util.UUID

/**
  * Created by lukewyman on 2/8/17.
  */

object RequestViews {


}

object ResponseViews {

  case class ReceivingSummaryView(
                                   productId: UUID,
                                   dateOrdered: ZonedDateTime,
                                   amountOrdered: Int,
                                   expectedDelivery: ZonedDateTime,
                                   delivered: ZonedDateTime,
                                   inStock: Int,
                                   onBackorder: Int
                                 )

}


