package com.ecommerce.common.views

import java.util.UUID

/**
  * Created by lukewyman on 2/5/17.
  */

object PaymentRequest {
  case class PaymentView(paymentId: UUID)
}

object PaymentResponse {
  case class PaymentTokenView(paymentId: UUID)
}
